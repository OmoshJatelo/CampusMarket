export const CLOUDINARY_CONFIG = {
  cloudName: import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'urf4pcg4',
  uploadPreset: import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'campusMarket',
  apiKey: import.meta.env.VITE_CLOUDINARY_API_KEY || '473959755772749',
  uploadUrl: `https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'urf4pcg4'}/image/upload`,
  maxSidePx: 1280,
  jpegQuality: 0.8,
  maxPhotos: 5,
} as const;

export function getCloudinaryUploadUrl(): string {
  return `https://api.cloudinary.com/v1_1/${CLOUDINARY_CONFIG.cloudName}/image/upload`;
}

export function getThumbnailUrl(url: string): string {
  if (!url) return '';
  const marker = '/upload/';
  const index = url.indexOf(marker);
  if (url.startsWith('https://res.cloudinary.com/') && index > 0) {
    const cut = index + marker.length;
    return url.substring(0, cut) + 'w_300,h_300,c_fill,q_auto/' + url.substring(cut);
  }
  return url;
}

export function getOptimizedUrl(url: string, width = 800): string {
  if (!url) return '';
  const marker = '/upload/';
  const index = url.indexOf(marker);
  if (url.startsWith('https://res.cloudinary.com/') && index > 0) {
    const cut = index + marker.length;
    return url.substring(0, cut) + `w_${width},q_auto,f_auto/` + url.substring(cut);
  }
  return url;
}