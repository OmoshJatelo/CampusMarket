import { BlockedUsersList } from '@components/moderation/BlockedUsersList';
import { useRealtimeBlockedUsers } from '@hooks/useModeration';
import { PageLayout } from '@components/layout/PageLayout';
import { UserMinus } from 'lucide-react';

export function BlockedUsersPage() {
  const { data: blockedUsers = [], isLoading } = useRealtimeBlockedUsers();

  return (
    <PageLayout>
      <div className="max-w-2xl mx-auto space-y-6">
        <div className="flex items-center gap-3">
          <UserMinus className="h-8 w-8 text-primary-600 dark:text-primary-400" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Blocked Sellers</h1>
            <p className="text-gray-500 dark:text-gray-400">You will no longer see their posts, move-out sales, or matches.</p>
          </div>
        </div>

        {isLoading ? (
          <div className="animate-pulse space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded-xl" />
            ))}
          </div>
        ) : (
          <BlockedUsersList blockedUsers={blockedUsers} />
        )}
      </div>
    </PageLayout>
  );
}