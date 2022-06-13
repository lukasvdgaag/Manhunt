package me.gaagjescraft.network.team.manhunt.utils;

import java.util.ArrayList;
import java.util.List;

public class LayoutUtils {
    public static List<Integer> getLayoutForItems(int count, int size) {
        if (size == 0 || size % 9 != 0) {
            throw new IllegalStateException("Incorrect size provided");
        }

        int itemCount = 0;
        int x = 1;
        int y = 1;
        if (size / 9 == 1) {
            y = 0;
        }

        List<Integer> result = new ArrayList<>();
        int itemsInRow = (9 - x) / 2;

        while (9 * y + x < size && itemCount < count) {
            int step = 2;
            int currentRowItemCount = itemsInRow;
            if (count - itemCount < currentRowItemCount) {
                switch (count - itemCount) {
                    case 1 -> {
                        x = 4;
                        step = 0;
                    }
                    case 2 -> {
                        x = 1;
                        step = 6;
                    }
                    case 3 -> {
                        x = 1;
                        step = 3;
                    }
                }
            }

            for (int i = 0; itemCount < count && i < currentRowItemCount; i++) {
                result.add(9 * y + x);
                itemCount++;
                x += step;
            }

            y += 1;
            // Move x to y to increase starting point for the next row
            //noinspection SuspiciousNameCombination
            x = y;
            itemsInRow--;
        }

        return result;
    }
}
