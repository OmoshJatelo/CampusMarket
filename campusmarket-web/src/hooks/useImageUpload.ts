import { useState, useCallback } from 'react';
import { uploadImages, validateImageFile, createObjectUrls, revokeObjectUrls, UploadResult } from '@lib/cloudinary/upload';
import { CLOUDINARY_CONFIG } from '@lib/cloudinary/config';

export interface UseImageUploadReturn {
  images: File[];
  previews: string[];
  uploading: boolean;
  progress: number;
  error: string | null;
  addImages: (files: File[]) => Promise<void>;
  removeImage: (index: number) => void;
  clearImages: () => void;
  upload: () => Promise<UploadResult[]>;
  cancelUpload: () => void;
  canAddMore: boolean;
}

export function useImageUpload(): UseImageUploadReturn {
  const [images, setImages] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [abortController, setAbortController] = useState<AbortController | null>(null);

  const canAddMore = images.length < CLOUDINARY_CONFIG.maxPhotos;

  const addImages = useCallback(async (newFiles: File[]) => {
    setError(null);
    const remainingSlots = CLOUDINARY_CONFIG.maxPhotos - images.length;
    const filesToAdd = newFiles.slice(0, remainingSlots);

    for (const file of filesToAdd) {
      const validationError = validateImageFile(file);
      if (validationError) {
        setError(validationError);
        return;
      }
    }

    const newImages = [...images, ...filesToAdd];
    const newPreviews = [...previews, ...createObjectUrls(filesToAdd)];
    setImages(newImages);
    setPreviews(newPreviews);
  }, [images, previews]);

  const removeImage = useCallback((index: number) => {
    revokeObjectUrls([previews[index]]);
    setImages((prev) => prev.filter((_, i) => i !== index));
    setPreviews((prev) => prev.filter((_, i) => i !== index));
  }, [previews]);

  const clearImages = useCallback(() => {
    revokeObjectUrls(previews);
    setImages([]);
    setPreviews([]);
  }, [previews]);

  const upload = useCallback(async (): Promise<UploadResult[]> => {
    if (images.length === 0) return [];

    setUploading(true);
    setProgress(0);
    setError(null);

    const controller = new AbortController();
    setAbortController(controller);

    try {
      const results = await uploadImages(images, {
        onProgress: (p) => setProgress(p.percentage),
        signal: controller.signal,
      });
      setUploading(false);
      setProgress(100);
      return results;
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        setError('Upload cancelled');
      } else {
        setError(err instanceof Error ? err.message : 'Upload failed');
      }
      setUploading(false);
      setProgress(0);
      throw err;
    }
  }, [images]);

  const cancelUpload = useCallback(() => {
    abortController?.abort();
    setUploading(false);
    setProgress(0);
  }, [abortController]);

  return {
    images,
    previews,
    uploading,
    progress,
    error,
    addImages,
    removeImage,
    clearImages,
    upload,
    cancelUpload,
    canAddMore,
  };
}