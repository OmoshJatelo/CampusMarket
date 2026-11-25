import { useBlockUser } from '@hooks/useModeration';
import { ConfirmDialog } from '@components/ui/Modal';
import { useToast } from '@components/ui/Toast';

interface BlockDialogProps {
  isOpen: boolean;
  onClose: () => void;
  sellerId: string;
  sellerLabel: string;
}

export function BlockDialog({ isOpen, onClose, sellerId, sellerLabel }: BlockDialogProps) {
  const { toast } = useToast();
  const { mutate: blockUser, isPending } = useBlockUser();

  const handleConfirm = () => {
    blockUser(
      { blockedId: sellerId, label: sellerLabel },
      {
        onSuccess: () => {
          toast({ type: 'success', message: 'Seller blocked.' });
          onClose();
        },
        onError: () => {
          toast({ type: 'error', message: 'Could not block seller. Please try again.' });
        },
      }
    );
  };

  return (
    <ConfirmDialog
      isOpen={isOpen}
      onClose={onClose}
      onConfirm={handleConfirm}
      title="Block this seller?"
      message={`You will no longer see their posts, move-out sales, or matches. You can undo this under Blocked sellers.`}
      confirmText="Block"
      cancelText="Cancel"
      variant="danger"
      loading={isPending}
    />
  );
}