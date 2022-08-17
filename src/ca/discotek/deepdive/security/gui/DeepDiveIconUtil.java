package ca.discotek.deepdive.security.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class DeepDiveIconUtil {
	
	public static final Image DEEPDIVE_ICONS[] = new Image[] {
	    createImage(16),
	    createImage(32),
	    createImage(42)
	};
	
	public static final List<Image> DEEPDIVE_ICON_LIST = Arrays.asList(DEEPDIVE_ICONS);
	
	static Image createImage(int dim) {
		BufferedImage image = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.black);
        Font font = new Font("Times New Roman", Font.BOLD, dim-2);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        LineMetrics lm = font.getLineMetrics("D", g.getFontRenderContext());
        
        int stringWidth = fm.stringWidth("D");
        int x = (int) (dim / 2 - stringWidth / 2);
        int y = (int) ((0+((dim+1-0)/2) - ((lm.getAscent() + lm.getDescent())/2) + lm.getAscent()));
        
        g.setColor(new Color(3, 113, 156));
        g.fillRect(0, 0, dim-1, dim-1);
        
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, dim-1, dim-1);
        
        g.setColor(Color.WHITE);
        g.drawString("D", x, y);
		
		return image;
	}
}
