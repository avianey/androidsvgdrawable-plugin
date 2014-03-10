package fr.avianey.mojo.androidgendrawable;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NinePatchStartStopTest {

    private final int start, stop, d;
    private final Density in, out;
    private final int expectedStart, expectedSize;

    public NinePatchStartStopTest(int start, int stop, int d, Density in, Density out, int expectedStart, int expectedSize) {
         this.start = start;
         this.stop = stop;
         this.d = d;
         this.in = in;
         this.out = out;
         this.expectedSize = expectedSize;
         this.expectedStart = expectedStart;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {0,     31,     32,     Density.mdpi,   Density.mdpi,     0,      32},
                        {0,     31,     48,     Density.mdpi,   Density.hdpi,     0,      48},
                        {0,     31,     64,     Density.mdpi,   Density.xhdpi,    0,      64},
                        {0,     31,     96,     Density.mdpi,   Density.xxhdpi,   0,      96},
                        {0,     31,     128,    Density.mdpi,   Density.xxxhdpi,  0,      128},
                        {0,     31,     24,     Density.mdpi,   Density.ldpi,     0,      18},
                        
                        {16,    31,     32,     Density.mdpi,   Density.mdpi,     16,     16},
                        {2,     31,     48,     Density.mdpi,   Density.hdpi,     3,      45},
                        {4,     31,     64,     Density.mdpi,   Density.xhdpi,    8,      56},
                        {8,     31,     96,     Density.mdpi,   Density.xxhdpi,   24,     72},
                        {16,    31,     128,    Density.mdpi,   Density.xxxhdpi,  64,     64},
                        {31,    31,     24,     Density.mdpi,   Density.ldpi,     17,     1},
                        
                        {16,    16,     32,     Density.mdpi,   Density.mdpi,     16,     1},
                        {2,     2,      48,     Density.mdpi,   Density.hdpi,     3,      1},
                        {4,     4,      64,     Density.mdpi,   Density.xhdpi,    8,      2},
                        {8,     8,      96,     Density.mdpi,   Density.xxhdpi,   24,     3},
                        {16,    16,     128,    Density.mdpi,   Density.xxxhdpi,  64,     4},
                        {32,    32,     24,     Density.mdpi,   Density.ldpi,     17,     1},
                        {2,     4,      48,     Density.mdpi,   Density.mdpi,     2,      3},
                        {2,     4,      48,     Density.mdpi,   Density.hdpi,     3,      4},
                        {4,     8,      64,     Density.mdpi,   Density.xhdpi,    8,      10},
                        {8,     16,     96,     Density.mdpi,   Density.xxhdpi,   24,     27},
                        {16,    32,     128,    Density.mdpi,   Density.xxxhdpi,  64,     68},
                        {8,     16,     24,     Density.mdpi,   Density.ldpi,     6,      6},
                        
                        {0,     255,    1024,   Density.mdpi,   Density.xxxhdpi,  0,      1024},
                        
                        // start & stop limit
                        {32,     32,    32,     Density.mdpi,   Density.mdpi,     31,      1},
                        {32,     32,    48,     Density.mdpi,   Density.hdpi,     48,      1},
                        {32,     32,    64,     Density.mdpi,   Density.xhdpi,    64,      2},
                        {32,     32,    96,     Density.mdpi,   Density.xxhdpi,   96,      3},
                        {32,     32,    128,    Density.mdpi,   Density.xxxhdpi,  128,     4},
                        {32,     32,    24,     Density.mdpi,   Density.ldpi,     17,      1},
                        {32,     32,    32,     Density.mdpi,   Density.mdpi,     31,      1},
                        {32,     32,    32,     Density.mdpi,   Density.hdpi,     47,      1},
                        {32,     32,    32,     Density.mdpi,   Density.xhdpi,    63,      1},
                        {32,     32,    32,     Density.mdpi,   Density.xxhdpi,   95,      1},
                        {32,     32,    32,     Density.mdpi,   Density.xxxhdpi,  127,     1},
                        {32,     32,    32,     Density.mdpi,   Density.ldpi,     23,      1},
                        
                        {32,     0,     32,     Density.mdpi,   Density.mdpi,     31,      1},
                        {32,     64,    32,     Density.mdpi,   Density.hdpi,     47,      1},
                        {32,     64,    48,     Density.mdpi,   Density.hdpi,     48,      24},
                        {32,     64,    64,     Density.mdpi,   Density.xhdpi,    64,      64},
                        {32,     64,    32,     Density.mdpi,   Density.xxhdpi,   95,      1},
                        {32,     64,    96,     Density.mdpi,   Density.xxhdpi,   96,      99},
                        {32,     64,    128,    Density.mdpi,   Density.xxxhdpi,  128,     132},
                        {32,     -8,    24,     Density.mdpi,   Density.ldpi,     17,      1},
                        {32,     0,     48,     Density.mdpi,   Density.hdpi,     48,      1},
                        {32,     0,     64,     Density.mdpi,   Density.xhdpi,    64,      1},
                        {32,     0,     96,     Density.mdpi,   Density.xxhdpi,   96,      1},
                        {32,     0,     128,    Density.mdpi,   Density.xxxhdpi,  128,     1},
                        {32,     0,     24,     Density.mdpi,   Density.ldpi,     17,      1},
                        
                        {16,    64,     32,     Density.mdpi,   Density.mdpi,     16,     16},
                        {2,     64,     48,     Density.mdpi,   Density.hdpi,     3,      69},
                        {2,     63,     64,     Density.mdpi,   Density.hdpi,     3,      93},
                        {4,     64,     64,     Density.mdpi,   Density.xhdpi,    8,      120},
                        {8,     64,     64,     Density.mdpi,   Density.xxhdpi,   24,     168},
                        {8,     64,     96,     Density.mdpi,   Density.xxhdpi,   24,     171},
                        {16,    64,     128,    Density.mdpi,   Density.xxxhdpi,  64,     196},
                        {32,    64,     24,     Density.mdpi,   Density.ldpi,     17,     1},
                        
                        // Issue #2
                        {4,     5,      10,     Density.mdpi,   Density.mdpi,     4,      2},
                        {4,     5,      12,     Density.mdpi,   Density.mdpi,     4,      2}
                        
                });
    }
    
    @Test
    public void start() {
    	final int computedStart = NinePatch.start(start, stop, d, in.ratio(out));
        Assert.assertEquals(expectedStart, computedStart);
        // start is in the box
        Assert.assertTrue("Start point is < 0", computedStart >= 0);
        Assert.assertTrue("Start point is outside boundaries", computedStart <= d * in.ratio(out));
    }
    
    @Test
    public void size() {
    	final int computedSize = NinePatch.size(start, stop, d, in.ratio(out));
        Assert.assertEquals(expectedSize, computedSize);
        Assert.assertTrue("Computed size is < 1", computedSize >= 1);
        // end is in the box
    	int computedStart = NinePatch.start(start, stop, d, in.ratio(out));
        Assert.assertTrue("End point is outside boundaries " + (computedStart + computedSize - 1) + " > " + (d * in.ratio(out)), computedStart + computedSize - 1 <= d * in.ratio(out));
    }

}
