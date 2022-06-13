package me.gaagjescraft.network.team.manhunt;

import me.gaagjescraft.network.team.manhunt.utils.LayoutUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LayoutUtilsTest {
    @Test
    public void generateLayout_For1Item_Success() {
        Assertions.assertEquals("....X....\n", stringify(1, 9));
    }

    @Test
    public void generateLayout_For5Items_Success() {
        Assertions.assertEquals(
                """
                        .........
                        .X.X.X.X.
                        ....X....
                        """,
                stringify(5, 27));
    }

    private String stringify(int count, int size) {
        char[] inventory = new char[size];
        for (int i = 0; i < size; i++) {
            inventory[i] = '.';
        }

        for (Integer layoutForItem : LayoutUtils.getLayoutForItems(count, size)) {
            inventory[layoutForItem] = 'X';
        }

        StringBuilder result = new StringBuilder();

        int y = 0;
        while (y * 9 < size) {
            for (int i = 0; i < 9; i++) {
                result.append(inventory[9 * y + i]);
            }

            result.append('\n');
            y++;
        }

        return result.toString();
    }
}
