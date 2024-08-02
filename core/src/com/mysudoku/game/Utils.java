package com.mysudoku.game;

import com.badlogic.gdx.utils.Array;
import com.mysudoku.game.gameboard.Cell;

public abstract class Utils {

    public static int numberToID(int number) {
        if (number == 0) {
            return 0;
        }
        return 0b1 << (number - 1);
    }

    public static int idToNumberSingle(int id) {
        if (id == 0)
            return 0;
        for (int i = 1; i <= 9; i++) {
            if (((id & 0b1 << (i - 1)) >> (i - 1)) == 1) {
                return i;
            }
        }
        return 0;
    }

    public static Array<Integer> idToNumber(int ids) {
        Array<Integer> numbers = new Array<>();
        if (ids == 0)
            return numbers;
        for (int i = 1; i <= 9; i++) {
            // Shift the 1 bit by i-1 then shift it back and if its 1, i is the number
            if (((ids & 0b1 << (i - 1)) >> (i - 1)) == 1) {
                numbers.add(i);
            }
        }
        if (numbers.isEmpty())
            numbers.add(0);
        return numbers;
    }

    public static boolean hasCandidate(Cell c, int candidate) {
        candidate = numberToID(candidate);
        int cellCandiate = c.getCandidates();
        return (candidate & cellCandiate) != 0;
    }

    public static void printArray(String preamble, int[] array) {
        System.out.println(preamble + ":");
        System.out.print("\t[");
        for (int i = 0; i < array.length - 1; i++) {
            System.out.printf("%d, ", array[i]);
        }
        System.out.print(array[array.length - 1] + "]");
    }

    public static void printArray(String preamble, int[] array, int colCount) {
        System.out.println(preamble + ":");
        System.out.print("\t[");
        for (int i = 0; i < array.length - 1; i++) {
            if (i % colCount == 0)
                System.out.println("\t");
            System.out.printf("%d, ", array[i]);
        }
        System.out.print(array[array.length - 1] + "]\n");
    }

    public static boolean areBoardsTheSame(int[] a, int[] b) {
        // Java can't be trusted
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] idArraytoIntArray(int[] ids) {
        int[] numbers = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            numbers[i] = idToNumberSingle(ids[i]);
        }
        return numbers;
    }

    public static int countCandidates(int candidates) {
        return Integer.bitCount(candidates);
    }

    public static String paddedBinary(int d, int numDigits) {
        String s = Integer.toBinaryString(d);
        StringBuffer sb = new StringBuffer(s);
        int numZeros = numDigits - s.length();
        while (numZeros-- > 0) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    public static int[] splitIDs(int id) {
        int[] array = new int[Integer.bitCount(id)];
        for (int j = 0; j < array.length; j++) {
            for (int i = 1; i <= 9; i++) {
                if (((id & 0b1 << (i - 1)) >> (i - 1)) == 1) {
                    array[j] = numberToID(i);
                    id = id & ~(0b1 << (i - 1));
                    break;
                }
            }
        }
        return array;
    }
}
