package fr.avianey.mojo.androidgendrawable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class SvgMaskTest {

	private static final String PATH_IN  = "./target/test-classes/" + SvgMaskTest.class.getSimpleName() + "/";
	private static final String PATH_OUT = "./target/generated-masked-resources/";
	
	private static int RUN = 0;

    private final String mask;
    private final List<QualifiedResource> resources;
    private final List<String> maskedResourcesNames;
    private final File dir;
    private final boolean useSameSvgOnlyOnceInMask;

    public SvgMaskTest(String mask, List<String> resourceNames, List<String> maskedResourcesNames,
    		boolean useSameSvgOnlyOnceInMask) {
		RUN++;
		this.dir = new File(PATH_OUT, String.valueOf(RUN));
		this.mask = mask;
		this.resources = new ArrayList<QualifiedResource>(resourceNames.size());
		for (String name : resourceNames) {
			this.resources.add(QualifiedResource.fromFile(new File(PATH_IN, name)));
		}
		this.maskedResourcesNames = maskedResourcesNames;
		this.useSameSvgOnlyOnceInMask = useSameSvgOnlyOnceInMask;
    }
    
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {
                        	"mask-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Arrays.asList("mask_square_red-mdpi.svg"),
                            false
                        },
                        {
                        	"mask_image_ns-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Arrays.asList("mask_image_ns_square_red-mdpi.svg"),
                            false
                        },
                        {
                        	"mask_image_ns_bad-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Collections.EMPTY_LIST,
                            false
                        },
                        {
                        	"mask_svg_ns-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Arrays.asList("mask_svg_ns_square_red-mdpi.svg"),
                            false
                        },
                        {
                        	"mask_no_image-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Collections.EMPTY_LIST,
                            false
                        },
                        {
                        	"mask_no_match-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Collections.EMPTY_LIST,
                            false
                        },
                        {
                        	"mask_no_regexp-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg"),
                            Collections.EMPTY_LIST,
                            false
                        },
                        {
                        	"mask_multiple_image-mdpi.svgmask", 
                        	Arrays.asList("square_red-mdpi.svg", "square_yellow-mdpi.svg", "circle_blue-mdpi.svg", "circle_green-mdpi.svg"),
                            Arrays.asList(
                            		"mask_multiple_image_square_red_circle_blue-mdpi.svg",
                            		"mask_multiple_image_square_red_circle_green-mdpi.svg",
                            		"mask_multiple_image_square_yellow_circle_blue-mdpi.svg",
                            		"mask_multiple_image_square_yellow_circle_green-mdpi.svg"
                            ),
                            false
                        },
                        {
                        	"mask_same_image_twice-mdpi.svgmask", 
                        	Arrays.asList(
                        			"square_red-mdpi.svg", "square_yellow-mdpi.svg", 
                        			"circle_blue-mdpi.svg", "circle_green-mdpi.svg", "circle_pink-mdpi.svg",
                        			"triangle_black-mdpi.svg", "triangle_white-mdpi.svg"
                        	),
                            Arrays.asList(
                            		"mask_same_image_twice_square_red_circle_blue_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_blue_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_blue_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_blue_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_white_square_yellow-mdpi.svg"
                            ),
                            false
                        },
                        {
                        	"mask_same_image_twice-mdpi.svgmask", 
                        	Arrays.asList(
                        			"square_red-mdpi.svg", "square_yellow-mdpi.svg", 
                        			"circle_blue-mdpi.svg", "circle_green-mdpi.svg", "circle_pink-mdpi.svg",
                        			"triangle_black-mdpi.svg", "triangle_white-mdpi.svg"
                        	),
                            Arrays.asList(
                            		"mask_same_image_twice_square_red_circle_blue_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_black_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_black_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_blue_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_green_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_red_circle_pink_triangle_white_square_yellow-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_blue_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_green_triangle_white_square_red-mdpi.svg",
                            		"mask_same_image_twice_square_yellow_circle_pink_triangle_white_square_red-mdpi.svg"
                            ),
                            true
                        }
                });
    }
    
    @Test
    public void fromJson() throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException  {
    	QualifiedResource maskResource = QualifiedResource.fromFile(new File(PATH_IN, mask));
    	SvgMask svgMask = new SvgMask(maskResource);
    	Collection<QualifiedResource> maskedResources = svgMask.generatesMaskedResources(dir, resources, useSameSvgOnlyOnceInMask);
    	Assert.assertEquals(maskedResourcesNames.size(), maskedResources.size());
    	Assert.assertEquals(maskedResourcesNames.size(), dir.list().length);
    	for (String maskedResource : maskedResourcesNames) {
    		Assert.assertTrue(new File(dir, maskedResource).exists());
    	}
    }
    
}
