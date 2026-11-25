import { useState } from 'react';
import { useReportListing } from '@hooks/useModeration';
import { AlertDialog } from '@components/ui/Modal';
import { REPORT_REASONS } from '@utils/constants';
import { useToast } from '@components/ui/Toast';

interface ReportDialogProps {
  isOpen: boolean;
  onClose: () => void;
  listingId: string;
  sellerId: string;
  listingTitle: string;
}

export function ReportDialog({ isOpen, onClose, listingId, sellerId, listingTitle }: ReportDialogProps) {
  const { toast } = useToast();
  const [selectedReason, setSelectedReason] = useState<string>('');
  const { mutate: reportListing, isPending } = useReportListing();

  const handleSubmit = () => {
    if (!selectedReason) return;

    reportListing(
      { listingId, sellerId, listingTitle, reason: selectedReason },
      {
        onSuccess: () => {
          toast({ type: 'success', message: 'Thanks. We will review this post.' });
          onClose();
          setSelectedReason('');
        },
        onError: () => {
          toast({ type: 'error', message: 'Could not send the report. You may have reported this post already.' });
        },
      }
    );
  };

  return (
    <AlertDialog
      isOpen={isOpen}
      onClose={onClose}
      title="Why are you reporting this?"
      actionText="Submit"
      onAction={handleSubmit}
      loading={isPending}
    >
      <div className="space-y-3 max-h-64 overflow-y-auto">
        {REPORT_REASONS.map((reason) => (
          <label
            key={reason}
            className="flex items-center gap-3 p-3 rounded-xl border transition-colors cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800"
          >
            <input
              type="radio"
              name="report-reason"
              value={reason}
              checked={selectedReason === reason}
              onChange={() => setSelectedReason(reason)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500"
            />
            <span className="text-gray-900 dark:text-white">{reason}</span>
          </label>
        ))}
      </div>
      <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">
        Your report is anonymous. The seller won't know who reported them.
      </p>
    </AlertDialog>
  );
}