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
                        {0,     32,     32,     Density.mdpi,   Density.mdpi,     0,      32},
                        {0,     32,     48,     Density.mdpi,   Density.hdpi,     0,      48},
                        {0,     32,     64,     Density.mdpi,   Density.xhdpi,    0,      64},
                        {0,     32,     96,     Density.mdpi,   Density.xxhdpi,   0,      96},
                        {0,     32,     128,    Density.mdpi,   Density.xxxhdpi,  0,      128},
                        {0,     32,     24,     Density.mdpi,   Density.ldpi,     0,      24},
                        
                        {16,    32,     32,     Density.mdpi,   Density.mdpi,     16,     16},
                        {2,     32,     48,     Density.mdpi,   Density.hdpi,     3,      45},
                        {4,     32,     64,     Density.mdpi,   Density.xhdpi,    8,      56},
                        {8,     32,     96,     Density.mdpi,   Density.xxhdpi,   24,     72},
                        {16,    32,     128,    Density.mdpi,   Density.xxxhdpi,  64,     64},
                        {32,    32,     24,     Density.mdpi,   Density.ldpi,     23,     1},
                        
                        {16,    16,     32,     Density.mdpi,   Density.mdpi,     16,     1},
                        {2,     2,      48,     Density.mdpi,   Density.hdpi,     3,      1},
                        {4,     4,      64,     Density.mdpi,   Density.xhdpi,    8,      1},
                        {8,     8,      96,     Density.mdpi,   Density.xxhdpi,   24,     1},
                        {16,    16,     128,    Density.mdpi,   Density.xxxhdpi,  64,     1},
                        {32,    32,     24,     Density.mdpi,   Density.ldpi,     23,     1},
                        {2,     4,      48,     Density.mdpi,   Density.hdpi,     3,      3},
                        {4,     8,      64,     Density.mdpi,   Density.xhdpi,    8,      8},
                        {8,     16,     96,     Density.mdpi,   Density.xxhdpi,   24,     24},
                        {16,    32,     128,    Density.mdpi,   Density.xxxhdpi,  64,     64},
                        {8,     16,     24,     Density.mdpi,   Density.ldpi,     6,      6},
                        
                        {0,     256,    1024,   Density.mdpi,   Density.xxxhdpi,  0,      1024},
                        
                        // start & stop limit
                        {32,     32,    32,     Density.mdpi,   Density.mdpi,     31,      1},
                        {32,     32,    48,     Density.mdpi,   Density.hdpi,     47,      1},
                        {32,     32,    64,     Density.mdpi,   Density.xhdpi,    63,      1},
                        {32,     32,    96,     Density.mdpi,   Density.xxhdpi,   95,      1},
                        {32,     32,    128,    Density.mdpi,   Density.xxxhdpi,  127,     1},
                        {32,     32,    24,     Density.mdpi,   Density.ldpi,     23,      1},
                        
                        {32,     0,     32,     Density.mdpi,   Density.mdpi,     31,      1},
                        {32,     64,    48,     Density.mdpi,   Density.hdpi,     47,      1},
                        {32,     64,    64,     Density.mdpi,   Density.xhdpi,    63,      1},
                        {32,     64,    96,     Density.mdpi,   Density.xxhdpi,   95,      1},
                        {32,     64,    128,    Density.mdpi,   Density.xxxhdpi,  127,     1},
                        {32,     -8,    24,     Density.mdpi,   Density.ldpi,     23,      1},
                        {32,     0,     48,     Density.mdpi,   Density.hdpi,     47,      1},
                        {32,     0,     64,     Density.mdpi,   Density.xhdpi,    63,      1},
                        {32,     0,     96,     Density.mdpi,   Density.xxhdpi,   95,      1},
                        {32,     0,     128,    Density.mdpi,   Density.xxxhdpi,  127,     1},
                        {32,     0,     24,     Density.mdpi,   Density.ldpi,     23,      1},
                        
                        {16,    64,     32,     Density.mdpi,   Density.mdpi,     16,     16},
                        {2,     64,     48,     Density.mdpi,   Density.hdpi,     3,      45},
                        {4,     64,     64,     Density.mdpi,   Density.xhdpi,    8,      56},
                        {8,     64,     96,     Density.mdpi,   Density.xxhdpi,   24,     72},
                        {16,    64,     128,    Density.mdpi,   Density.xxxhdpi,  64,     64},
                        {32,    64,     24,     Density.mdpi,   Density.ldpi,     23,     1}
                        
                });
    }
    
    @Test
    public void start() {
        Assert.assertEquals(expectedStart, NinePatch.start(start, stop, d, in.ratio(out)));
    }
    
    @Test
    public void size() {
        Assert.assertEquals(expectedSize, NinePatch.size(start, stop, d, in.ratio(out)));
    }

}
