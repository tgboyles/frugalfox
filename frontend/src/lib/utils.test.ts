import { describe, it, expect } from 'vitest';
import { cn } from '@/lib/utils';

describe('Utils', () => {
  describe('cn', () => {
    it('cn_SingleClassName_ReturnsClassName', () => {
      // Arrange
      const className = 'text-red-500';

      // Act
      const result = cn(className);

      // Assert
      expect(result).toBe('text-red-500');
    });

    it('cn_MultipleClassNames_MergesTailwindClasses', () => {
      // Arrange
      const classes = ['text-red-500', 'text-blue-500'];

      // Act
      const result = cn(...classes);

      // Assert
      // tailwind-merge should keep only the last color class
      expect(result).toBe('text-blue-500');
    });

    it('cn_ConditionalClasses_FiltersOutFalsyValues', () => {
      // Arrange
      const isActive = false;
      const isDisabled = true;

      // Act
      const result = cn('base-class', isActive && 'active', isDisabled && 'disabled');

      // Assert
      expect(result).toBe('base-class disabled');
    });

    it('cn_EmptyInput_ReturnsEmptyString', () => {
      // Arrange & Act
      const result = cn();

      // Assert
      expect(result).toBe('');
    });

    it('cn_ObjectInput_MergesObjectKeys', () => {
      // Arrange
      const classObject = {
        'text-red-500': true,
        'bg-blue-500': false,
        'p-4': true,
      };

      // Act
      const result = cn(classObject);

      // Assert
      expect(result).toContain('text-red-500');
      expect(result).toContain('p-4');
      expect(result).not.toContain('bg-blue-500');
    });

    it('cn_ArrayInput_MergesArrayElements', () => {
      // Arrange
      const classArray = ['flex', 'items-center', 'justify-between'];

      // Act
      const result = cn(classArray);

      // Assert
      expect(result).toBe('flex items-center justify-between');
    });

    it('cn_MixedInput_HandlesAllInputTypes', () => {
      // Arrange
      const condition = true;
      const classes = [
        'base',
        condition && 'conditional',
        { active: true, disabled: false },
        'final',
      ];

      // Act
      const result = cn(...classes);

      // Assert
      expect(result).toContain('base');
      expect(result).toContain('conditional');
      expect(result).toContain('active');
      expect(result).toContain('final');
      expect(result).not.toContain('disabled');
    });

    it('cn_ConflictingTailwindClasses_KeepsLastOne', () => {
      // Arrange - conflicting padding classes
      const classes = 'p-2 p-4 p-8';

      // Act
      const result = cn(classes);

      // Assert
      // tailwind-merge should keep only the last padding class
      expect(result).toBe('p-8');
    });

    it('cn_NullAndUndefined_FiltersThemOut', () => {
      // Arrange
      const classes = ['text-sm', null, 'font-bold', undefined, 'text-center'];

      // Act
      const result = cn(...classes);

      // Assert
      expect(result).toBe('text-sm font-bold text-center');
    });
  });
});
