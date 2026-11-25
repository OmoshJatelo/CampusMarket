import { useUnblockUser } from '@hooks/useModeration';
import { useToast } from '@components/ui/Toast';
import { Button } from '@components/ui/Button';
import { X, User } from 'lucide-react';
import { BlockedUser } from '@models/BlockedUser';
import { clsx } from 'clsx';

interface BlockedUsersListProps {
  blockedUsers: BlockedUser[];
}

export function BlockedUsersList({ blockedUsers }: BlockedUsersListProps) {
  const { toast } = useToast();
  const { mutate: unblockUser, isPending } = useUnblockUser();

  if (blockedUsers.length === 0) {
    return (
      <div className="text-center py-12">
        <User className="h-16 w-16 mx-auto text-gray-300 dark:text-gray-600 mb-4" />
        <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No blocked sellers</h3>
        <p className="text-gray-500 dark:text-gray-400">When you block a seller, they'll appear here.</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {blockedUsers.map((blocked) => (
        <div
          key={blocked.id}
          className={clsx(
            'flex items-center justify-between p-4 rounded-xl border transition-colors',
            'bg-white border-gray-200 dark:bg-gray-800 dark:border-gray-700'
          )}
        >
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
              <User className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            </div>
            <div>
              <p className="font-medium text-gray-900 dark:text-white">
                Seller of {blocked.label || 'a listing'}
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Blocked {new Date(blocked.createdAt).toLocaleDateString()}
              </p>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              unblockUser(
                blocked.id,
                {
                  onSuccess: () => {
                    toast({ type: 'success', message: 'Seller unblocked.' });
                  },
                  onError: () => {
                    toast({ type: 'error', message: 'Failed to unblock seller.' });
                  },
                }
              );
            }}
            disabled={isPending}
            className="text-red-600 hover:text-red-700 dark:text-red-400"
          >
            <X className="h-4 w-4 mr-1.5" />
            Unblock
          </Button>
        </div>
      ))}
    </div>
  );
}