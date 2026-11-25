import { Product, isWanted, isActive } from './Product';

export interface Match {
  mine: Product;
  other: Product;
  key: string;
}

const STOP_WORDS = new Set([
  'the', 'and', 'for', 'with', 'used', 'new', 'good', 'looking', 'wanted',
  'want', 'need', 'sale', 'selling', 'cheap', 'very', 'any', 'one',
]);

function extractKeywords(text: string): Set<string> {
  if (!text) return new Set();
  const words = text.toLowerCase().split(/[^a-z0-9]+/);
  const keywords = new Set<string>();
  for (const word of words) {
    if (word.length < 3) continue;
    if (STOP_WORDS.has(word)) continue;
    // Simple stemming: remove trailing 's'
    let stemmed = word;
    if (word.length > 3 && word.endsWith('s')) {
      stemmed = word.slice(0, -1);
    }
    keywords.add(stemmed);
  }
  return keywords;
}

export function fits(wanted: Product, sale: Product): boolean {
  if (!isWanted(wanted) || isWanted(sale)) return false;
  if (sale.price > wanted.price) return false; // Over budget

  const wc = wanted.category;
  const sc = sale.category;
  const bothHaveCategory = wc && sc && wc !== '' && sc !== '';
  if (bothHaveCategory && wc.toLowerCase() !== sc.toLowerCase()) return false;

  const wantedWords = extractKeywords(wanted.title);
  const saleWords = extractKeywords(`${sale.title} ${sale.description}`);
  // Check intersection
  for (const word of wantedWords) {
    if (saleWords.has(word)) return true;
  }
  return false;
}

export function findMatches(userId: string, allProducts: Product[]): Match[] {
  if (!userId) return [];

  const matches: Match[] = [];
  const userProducts = allProducts.filter((p) => p.ownerId === userId && isActive(p));
  const otherProducts = allProducts.filter((p) => p.ownerId !== userId && isActive(p));

  for (const mine of userProducts) {
    for (const other of otherProducts) {
      let match: Match | null = null;
      if (isWanted(mine) && !isWanted(other)) {
        if (fits(mine, other)) {
          match = { mine, other, key: `${mine.id}|${other.id}` };
        }
      } else if (!isWanted(mine) && isWanted(other)) {
        if (fits(other, mine)) {
          match = { mine, other, key: `${mine.id}|${other.id}` };
        }
      }
      if (match) matches.push(match);
    }
  }

  return matches;
}

export function getUniqueMatchedProducts(matches: Match[]): Product[] {
  const seen = new Set<string>();
  const unique: Product[] = [];
  for (const match of matches) {
    if (!seen.has(match.other.id)) {
      seen.add(match.other.id);
      unique.push(match.other);
    }
  }
  return unique;
}

export function getMatchCountForBadge(userId: string, allProducts: Product[]): number {
  const matches = findMatches(userId, allProducts);
  return getUniqueMatchedProducts(matches).length;
}