import { describe, it, expect } from 'vitest';
import { QUOTES, type Quote } from '@/lib/quotes';

describe('Quotes', () => {
  describe('QUOTES data validation', () => {
    it('QUOTES_Array_IsNotEmpty', () => {
      // Act & Assert
      expect(QUOTES).toBeDefined();
      expect(QUOTES.length).toBeGreaterThan(0);
    });

    it('QUOTES_AllEntries_HaveRequiredFields', () => {
      // Act & Assert
      QUOTES.forEach((quote: Quote, index: number) => {
        expect(quote.text, `Quote at index ${index} should have text`).toBeDefined();
        expect(quote.text, `Quote at index ${index} should have non-empty text`).not.toBe('');
        expect(quote.author, `Quote at index ${index} should have author`).toBeDefined();
        expect(quote.author, `Quote at index ${index} should have non-empty author`).not.toBe('');
      });
    });

    it('QUOTES_AllEntries_HaveStringFields', () => {
      // Act & Assert
      QUOTES.forEach((quote: Quote, index: number) => {
        expect(typeof quote.text, `Quote text at index ${index} should be a string`).toBe('string');
        expect(typeof quote.author, `Quote author at index ${index} should be a string`).toBe(
          'string'
        );
      });
    });

    it('QUOTES_AllEntries_AreTrimmed', () => {
      // Act & Assert
      QUOTES.forEach((quote: Quote, index: number) => {
        expect(quote.text, `Quote text at index ${index} should not have leading whitespace`).toBe(
          quote.text.trim()
        );
        expect(
          quote.author,
          `Quote author at index ${index} should not have trailing whitespace`
        ).toBe(quote.author.trim());
      });
    });

    it('QUOTES_ContainsExpectedQuote_BenjaminFranklin', () => {
      // Arrange
      const expectedQuote = 'A penny saved is a penny earned.';
      const expectedAuthor = 'Benjamin Franklin';

      // Act
      const found = QUOTES.find((q) => q.text === expectedQuote && q.author === expectedAuthor);

      // Assert
      expect(found).toBeDefined();
    });

    it('QUOTES_ContainsExpectedQuote_WarrenBuffett', () => {
      // Arrange
      const expectedText =
        'Do not save what is left after spending, but spend what is left after saving.';
      const expectedAuthor = 'Warren Buffett';

      // Act
      const found = QUOTES.find((q) => q.text === expectedText && q.author === expectedAuthor);

      // Assert
      expect(found).toBeDefined();
    });

    it('QUOTES_NoEntries_HaveDuplicateTextAndAuthor', () => {
      // Arrange
      const seen = new Set<string>();

      // Act & Assert
      QUOTES.forEach((quote: Quote, index: number) => {
        const key = `${quote.text}|${quote.author}`;
        expect(
          seen.has(key),
          `Quote at index ${index} is a duplicate: "${quote.text}" - ${quote.author}`
        ).toBe(false);
        seen.add(key);
      });
    });

    it('QUOTES_Export_MatchesTypeDefinition', () => {
      // Act
      const firstQuote = QUOTES[0];

      // Assert - TypeScript will enforce this, but we verify at runtime too
      expect(firstQuote).toHaveProperty('text');
      expect(firstQuote).toHaveProperty('author');
      expect(Object.keys(firstQuote).sort()).toEqual(['author', 'text']);
    });
  });
});
