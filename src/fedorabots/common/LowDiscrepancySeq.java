package fedorabots.common;

import javafx.geometry.Point2D;

/**
 * Implementations of various low discrepancy sequences for use in placing
 * robots and obstacles.
 */
public class LowDiscrepancySeq {

    private static double halton(int n, int b) {
        double r = 0;
        double baseTerm = 1;
        while (n > 0) {
            baseTerm /= b;
            r += baseTerm * (n % b);
            n /= b;
        }
        return r;
    }

    public static Point2D haltonPoint(int n) {
        return new Point2D(halton(n, 2), halton(n, 3));
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            System.out.println(halton(i, 10));
        }
    }

}
