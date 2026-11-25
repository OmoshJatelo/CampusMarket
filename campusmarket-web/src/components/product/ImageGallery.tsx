import { useState, useCallback, useRef, useEffect } from 'react';
import { clsx } from 'clsx';
import { getOptimizedUrl } from '@lib/cloudinary/config';
import { Dots } from '@components/common/Dots';
import { ZoomImageView } from './ZoomImageView';

interface ImageGalleryProps {
  images: string[];
  initialIndex?: number;
  onClose?: () => void;
  fullScreen?: boolean;
}

export function ImageGallery({ images, initialIndex = 0, onClose, fullScreen = false }: ImageGalleryProps) {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const [touchStart, setTouchStart] = useState<number | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const goToNext = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % images.length);
  }, [images.length]);

  const goToPrev = useCallback(() => {
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  }, [images.length]);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'ArrowRight') goToNext();
      if (e.key === 'ArrowLeft') goToPrev();
      if (e.key === 'Escape' && onClose) onClose();
    },
    [goToNext, goToPrev, onClose]
  );

  const handleTouchStart = (e: React.TouchEvent) => {
    setTouchStart(e.touches[0].clientX);
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (touchStart === null) return;
    const diff = e.changedTouches[0].clientX - touchStart;
    if (Math.abs(diff) > 50) {
      if (diff > 0) goToPrev();
      else goToNext();
    }
    setTouchStart(null);
  };

  useEffect(() => {
    if (fullScreen) {
      document.addEventListener('keydown', handleKeyDown);
      return () => document.removeEventListener('keydown', handleKeyDown);
    }
  }, [fullScreen, handleKeyDown]);

  if (images.length === 0) return null;

  const currentImage = images[currentIndex];
  const optimizedUrl = getOptimizedUrl(currentImage, fullScreen ? 1600 : 800);

  return (
    <div
      ref={containerRef}
      className={clsx(
        'relative',
        fullScreen ? 'fixed inset-0 z-50 bg-black' : 'rounded-2xl overflow-hidden bg-gray-100 dark:bg-gray-700'
      )}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {fullScreen && onClose && (
        <button
          type="button"
          onClick={onClose}
          className="absolute top-4 right-4 z-10 p-2 rounded-full bg-white/20 text-white hover:bg-white/30 transition-colors"
          aria-label="Close gallery"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      )}

      <div className={clsx('relative', fullScreen ? 'h-full' : 'aspect-[4/3]')}>
        {fullScreen ? (
          <ZoomImageView
            src={optimizedUrl}
            alt={`Image ${currentIndex + 1} of ${images.length}`}
            onClose={onClose}
          />
        ) : (
          <img
            src={optimizedUrl}
            alt={`Image ${currentIndex + 1} of ${images.length}`}
            className="w-full h-full object-cover"
            loading={currentIndex === initialIndex ? 'eager' : 'lazy'}
          />
        )}

        {images.length > 1 && !fullScreen && (
          <>
            <button
              type="button"
              onClick={goToPrev}
              className="absolute left-2 top-1/2 -translate-y-1/2 p-1.5 rounded-full bg-white/80 dark:bg-gray-800/80 text-gray-700 dark:text-gray-300 hover:bg-white dark:hover:bg-gray-700 transition-colors shadow-sm"
              aria-label="Previous image"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <button
              type="button"
              onClick={goToNext}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-full bg-white/80 dark:bg-gray-800/80 text-gray-700 dark:text-gray-300 hover:bg-white dark:hover:bg-gray-700 transition-colors shadow-sm"
              aria-label="Next image"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </>
        )}

        {fullScreen && images.length > 1 && (
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex items-center gap-4">
            <button
              type="button"
              onClick={goToPrev}
              className="p-2 rounded-full bg-white/20 text-white hover:bg-white/30 transition-colors"
              aria-label="Previous"
            >
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <Dots count={images.length} activeIndex={currentIndex} size="md" />
            <button
              type="button"
              onClick={goToNext}
              className="p-2 rounded-full bg-white/20 text-white hover:bg-white/30 transition-colors"
              aria-label="Next"
            >
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        )}
      </div>

      {!fullScreen && images.length > 1 && (
        <div className="flex justify-center gap-1.5 p-3">
          {images.map((_, i) => (
            <button
              key={i}
              type="button"
              onClick={() => setCurrentIndex(i)}
              className={clsx(
                'h-2 w-2 rounded-full transition-all duration-200',
                i === currentIndex
                  ? 'bg-primary-600 w-6'
                  : 'bg-gray-300 dark:bg-gray-600 hover:bg-gray-400 dark:hover:bg-gray-500'
              )}
              aria-label={`Go to image ${i + 1}`}
              aria-current={i === currentIndex ? 'true' : 'false'}
            />
          ))}
        </div>
      )}
    </div>
  );
}