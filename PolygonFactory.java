package SnakeGameProject;

import java.awt.Polygon;

public class PolygonFactory {
    // Polygon p;

    int sides;
    double centerX;
    double centerY;

    // dependent on user choice of board size
    int scaleFactor;
    int patternDepth;

    public PolygonFactory(int sides, double centerX, double centerY, int scaleFactor, int patternDepth) {
        this.sides = sides;
        this.centerX = centerX;
        this.centerY = centerY;
        this.scaleFactor = scaleFactor;
        this.patternDepth = patternDepth;
    }

    // polygon without internal pattern
    // public PolygonFactory(int sides, double centerX, double centerY, int scaleFactor) {
    //    this.sides = sides;
    //    this.centerX = centerX;
    //    this.centerY = centerY;
    //    this.scaleFactor = scaleFactor;
    // }

    // manually create polygon
    // public Polygon create() {
    //    p = new Polygon();
    //    for (int i = 0; i < sides; i++)
    //        p.addPoint( (int)(centerX + (double)scaleFactor * Math.cos((2.0 * Math.PI * (double)i) / (double)sides)),
    //                    (int)(centerY + (double)scaleFactor * Math.sin((2.0 * Math.PI * (double)i) / (double)sides)));
    //    return p;
    // }

    public Polygon[] bullsEyePattern(double scaleDecreaseFactor) {
        Polygon [] ps = new Polygon[patternDepth];
        Polygon q;

        for(int j = 0, scale = scaleFactor; j < patternDepth; j++, scale -= scaleDecreaseFactor) {
            q = new Polygon();

            for (int i = 0; i < sides; i++) {
                q.addPoint((int) (centerX + (double)scale * Math.cos((2.0 * Math.PI * (double)i) / (double)sides)),
                           (int) (centerY + (double)scale * Math.sin((2.0 * Math.PI * (double)i) / (double)sides)));
            }

            ps[j] = q;
        }

        return ps;
    }

    public static Polygon nDegreeRotation(Polygon q, double rad, double cX, double cY) {
        for ( int i = 0, x, y; i < q.npoints; i++ ) {
            x = (int)((double)q.xpoints[i] - cX);
            y = (int)((double)q.ypoints[i] - cY);

            q.xpoints[i] = (int)(((double)x * Math.cos(rad) - (double)y * Math.sin(rad)) + cX);
            q.ypoints[i] = (int)(((double)x * Math.sin(rad) + (double)y * Math.cos(rad)) + cY);
        }

        return q;
    }
}