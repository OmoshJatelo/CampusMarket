import imageCompression from 'browser-image-compression';
import { getCloudinaryUploadUrl, CLOUDINARY_CONFIG } from './config';

export interface UploadProgress {
  loaded: number;
  total: number;
  percentage: number;
}

export interface UploadResult {
  url: string;
  publicId: string;
  width: number;
  height: number;
  format: string;
}

export interface UploadOptions {
  onProgress?: (progress: UploadProgress) => void;
  signal?: AbortSignal;
}

async function compressImage(file: File): Promise<File> {
  const options = {
    maxSizeMB: 1,
    maxWidthOrHeight: CLOUDINARY_CONFIG.maxSidePx,
    useWebWorker: true,
    fileType: 'image/jpeg',
    quality: CLOUDINARY_CONFIG.jpegQuality,
  };

  try {
    const compressedBlob = await imageCompression(file, options);
    return new File([compressedBlob], file.name.replace(/\.[^.]+$/, '.jpg'), {
      type: 'image/jpeg',
      lastModified: Date.now(),
    });
  } catch {
    // If compression fails, return original file
    return file;
  }
}

function createFormData(file: File): FormData {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', CLOUDINARY_CONFIG.uploadPreset);
  return formData;
}

export async function uploadImage(
  file: File,
  options: UploadOptions = {}
): Promise<UploadResult> {
  const { signal } = options;

  // Compress first
  const compressedFile = await compressImage(file);
  const formData = createFormData(compressedFile);

  const response = await fetch(getCloudinaryUploadUrl(), {
    method: 'POST',
    body: formData,
    signal,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.error?.message || `Upload failed: ${response.status}`);
  }

  const data = await response.json();
  return {
    url: data.secure_url,
    publicId: data.public_id,
    width: data.width,
    height: data.height,
    format: data.format,
  };
}

export async function uploadImages(
  files: File[],
  options: UploadOptions = {}
): Promise<UploadResult[]> {
  const { onProgress, signal } = options;
  const results: UploadResult[] = [];

  for (let i = 0; i < files.length; i++) {
    if (signal?.aborted) {
      throw new Error('Upload aborted');
    }

    const file = files[i];
    const result = await uploadImage(file, {
      onProgress: (progress) => {
        // Report overall progress across all files
        const overallProgress: UploadProgress = {
          loaded: i * 100 + progress.percentage,
          total: files.length * 100,
          percentage: Math.round((i * 100 + progress.percentage) / files.length),
        };
        onProgress?.(overallProgress);
      },
      signal,
    });

    results.push(result);
  }

  return results;
}

export function validateImageFile(file: File): string | null {
  const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif'];
  if (!validTypes.includes(file.type)) {
    return 'Please select a valid image file (JPEG, PNG, WebP, HEIC)';
  }

  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    return 'Image must be smaller than 10MB';
  }

  return null;
}

export function createObjectUrls(files: File[]): string[] {
  return files.map((file) => URL.createObjectURL(file));
}

export function revokeObjectUrls(urls: string[]): void {
  urls.forEach((url) => URL.revokeObjectURL(url));
}