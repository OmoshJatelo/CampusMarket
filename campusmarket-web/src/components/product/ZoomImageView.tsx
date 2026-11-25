import { useState, useRef, useEffect, useCallback } from 'react';
import { clsx } from 'clsx';

interface ZoomImageViewProps {
  src: string;
  alt: string;
  onClose?: () => void;
}

export function ZoomImageView({ src, alt, onClose }: ZoomImageViewProps) {
  const [scale, setScale] = useState(1);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const imageRef = useRef<HTMLImageElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const MAX_SCALE = 4;
  const MIN_SCALE = 1;

  const resetView = useCallback(() => {
    setScale(1);
    setPosition({ x: 0, y: 0 });
  }, []);

  const handleWheel = useCallback(
    (e: React.WheelEvent) => {
      e.preventDefault();
      const delta = -e.deltaY * 0.001;
      setScale((prev) => {
        const newScale = Math.min(Math.max(prev + delta, MIN_SCALE), MAX_SCALE);
        return newScale;
      });
    },
    []
  );

  const handleMouseDown = useCallback(
    (e: React.MouseEvent) => {
      if (scale <= 1) return;
      setIsDragging(true);
      setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y });
      e.preventDefault();
    },
    [scale, position]
  );

  const handleTouchStart = useCallback(
    (e: React.TouchEvent) => {
      if (scale <= 1) return;
      const touch = e.touches[0];
      setIsDragging(true);
      setDragStart({ x: touch.clientX - position.x, y: touch.clientY - position.y });
    },
    [scale, position]
  );

  const handleMove = useCallback(
    (clientX: number, clientY: number) => {
      if (!isDragging) return;
      setPosition({
        x: clientX - dragStart.x,
        y: clientY - dragStart.y,
      });
    },
    [isDragging, dragStart]
  );

  const handleMouseMove = useCallback(
    (e: React.MouseEvent) => handleMove(e.clientX, e.clientY),
    [handleMove]
  );

  const handleTouchMove = useCallback(
    (e: React.TouchEvent) => {
      if (e.touches.length === 1) {
        handleMove(e.touches[0].clientX, e.touches[0].clientY);
      }
    },
    [handleMove]
  );

  const handleMouseUp = useCallback(() => {
    setIsDragging(false);
  }, []);

  const handleTouchEnd = useCallback(() => {
    setIsDragging(false);
  }, []);

  const handleDoubleClick = useCallback(() => {
    if (scale > 1) {
      resetView();
    } else {
      setScale(2.5);
    }
  }, [scale, resetView]);

  // Clamp position to keep image in bounds
  useEffect(() => {
    if (!imageRef.current || !containerRef.current) return;

    const img = imageRef.current;
    const container = containerRef.current;

    const imgWidth = img.naturalWidth * scale;
    const imgHeight = img.naturalHeight * scale;
    const containerWidth = container.clientWidth;
    const containerHeight = container.clientHeight;

    const maxX = Math.max(0, (imgWidth - containerWidth) / 2);
    const maxY = Math.max(0, (imgHeight - containerHeight) / 2);

    setPosition((prev) => ({
      x: Math.max(-maxX, Math.min(maxX, prev.x)),
      y: Math.max(-maxY, Math.min(maxY, prev.y)),
    }));
  }, [scale, position]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && onClose) onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div
      ref={containerRef}
      className="w-full h-full overflow-hidden cursor-grab cursor-[grab]"
      onWheel={handleWheel}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
      onDoubleClick={handleDoubleClick}
    >
      <img
        ref={imageRef}
        src={src}
        alt={alt}
        className={clsx(
          'transition-transform duration-100',
          isDragging && 'cursor-grabbing cursor-[grabbing]'
        )}
        style={{
          transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
          transformOrigin: 'center center',
          willChange: 'transform',
        }}
        loading="eager"
      />
    </div>
  );
}