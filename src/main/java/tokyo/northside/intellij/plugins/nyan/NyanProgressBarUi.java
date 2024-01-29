package tokyo.northside.intellij.plugins.nyan;

import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.*;
import com.intellij.ui.*;
import com.intellij.util.ui.*;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class NyanProgressBarUi extends BasicProgressBarUI {
    private static final String PACKAGE_PATH = "/tokyo/northside/intellij/plugins/nyan/";
    private final ScalableIcon CAT_ICON = (ScalableIcon) IconLoader.getIcon(PACKAGE_PATH + "rsz_cat.png", getClass().getClassLoader());
    private final ScalableIcon RCAT_ICON = (ScalableIcon) IconLoader.getIcon(PACKAGE_PATH + "rsz_rcat.png", getClass().getClassLoader());

    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent c) {
        c.setBorder(JBUI.Borders.empty().asUIResource());
        return new NyanProgressBarUi();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(super.getPreferredSize(c).width, JBUI.scale(20));
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        progressBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
            }
        });
    }

    private volatile int offset = 0;
    private volatile int offset2 = 0;
    private volatile int velocity = 1;

    @Override
    protected void paintIndeterminate(Graphics g2d, JComponent c) {
        if (!(g2d instanceof Graphics2D g)) {
            return;
        }

        Insets b = progressBar.getInsets(); // area for a border
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        g.setColor(new JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50)));
        int w = c.getWidth();
        int h = c.getPreferredSize().height;
        if (isOdd(c.getHeight() - h)) {
            h++;
        }

        LinearGradientPaint baseRainbowPaint = getRainbowPaintFromHeight(h);


        g.setPaint(baseRainbowPaint);

        if (c.isOpaque()) {
            g.fillRect(0, (c.getHeight() - h) / 2, w, h);
        }
        g.setColor(new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50)));
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        g.translate(0, (c.getHeight() - h) / 2);
        Paint old = g.getPaint();
        g.setPaint(baseRainbowPaint);

        final float R = JBUI.pixScale(8f);
        final float R2 = JBUI.pixScale(9f);
        final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R));
        g.fill(containingRoundRect);
        g.setPaint(old);
        synchronized (this) {
            offset = (offset + 1) % getPeriodLength();
            offset2 += velocity;
            if (offset2 <= 2) {
                offset2 = 2;
                velocity = 1;
            } else if (offset2 >= w - JBUI.scale(15)) {
                offset2 = w - JBUI.scale(15);
                velocity = -1;
            }
        }
        Area area = new Area(new Rectangle2D.Float(0, 0, w, h));
        area.subtract(new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R)));
        g.setPaint(Gray._128);
        if (c.isOpaque()) {
            g.fill(area);
        }

        area.subtract(new Area(new RoundRectangle2D.Float(0, 0, w, h, R2, R2)));

        Container parent = c.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
        g.setPaint(background);
        if (c.isOpaque()) {
            g.fill(area);
        }

        Icon scaledIcon = velocity > 0 ? CAT_ICON : RCAT_ICON;
        scaledIcon.paintIcon(progressBar, g, offset2 - JBUI.scale(10), -JBUI.scale(6));

        g.draw(new RoundRectangle2D.Float(1f, 1f, w - 2f - 1f, h - 2f - 1f, R, R));
        g.translate(0, -(c.getHeight() - h) / 2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width);
            } else {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.y, boxRect.height);
            }
        }
        config.restore();
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D g2)) {
            return;
        }

        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL || !c.getComponentOrientation().isLeftToRight()) {
            super.paintDeterminate(g, c);
            return;
        }
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        Insets b = progressBar.getInsets(); // area for a border
        int w = progressBar.getWidth();
        int h = progressBar.getPreferredSize().height;
        if (isOdd(c.getHeight() - h)) h++;

        int barRectWidth = w - (b.right + b.left);
        int barRectHeight = h - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        Container parent = c.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

        g.setColor(background);
        if (c.isOpaque()) {
            g.fillRect(0, 0, w, h);
        }

        final float R = JBUI.pixScale(8f);
        final float R2 = JBUI.pixScale(9f);
        final float off = JBUI.pixScale(1f);

        g2.translate(0, (c.getHeight() - h) / 2);
        g2.setColor(progressBar.getForeground());
        g2.fill(new RoundRectangle2D.Float(0, 0, w - off, h - off, R2, R2));
        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Float(off, off, w - 2f * off - off, h - 2f * off - off, R, R));
        g2.setPaint(getRainbowPaintFromHeight(h));

        CAT_ICON.paintIcon(progressBar, g2, amountFull - JBUI.scale(10), -JBUI.scale(6));
        g2.fill(new RoundRectangle2D.Float(2f * off, 2f * off, amountFull - JBUI.pixScale(5f), h - JBUI.pixScale(5f), JBUI.pixScale(7f), JBUI.pixScale(7f)));
        g2.translate(0, -(c.getHeight() - h) / 2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
        }
        config.restore();
    }

    private void paintString(Graphics g, int x, int y, int w, int h, int fillStart, int amountFull) {
        if (!(g instanceof Graphics2D g2)) {
            return;
        }
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
        Rectangle oldClip = g2.getClipBounds();

        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
            g2.setColor(getSelectionBackground());
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
        } else { // VERTICAL
            g2.setColor(getSelectionBackground());
            AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x, fillStart, w, amountFull);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    /**
     * Create a gradient such as [0, 0.99, 1, 1.99, ...], [RED, RED, ORANGE, ORANGE, ..]
     */
    private LinearGradientPaint getRainbowPaintFromHeight(float scaledHeight) {

        int numRainbowColors = RainbowColors.RAINBOW_ARRAY.length;
        float epsilon = 0.000001f;

        float[] fractionList = new float[numRainbowColors * 2];
        Color[] colorList = new Color[numRainbowColors * 2];

        for (int i = 0; i < numRainbowColors; i++) {
            fractionList[i * 2] = (float) i / numRainbowColors;
            fractionList[i * 2 + 1] = ((i + 1) - epsilon) / numRainbowColors;

            colorList[i * 2] = RainbowColors.RAINBOW_ARRAY[i];
            colorList[i * 2 + 1] = RainbowColors.RAINBOW_ARRAY[i];
        }

        return new LinearGradientPaint(0, JBUI.scale(1), 0, scaledHeight - JBUI.scale(3), fractionList, colorList);
    }

    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    private int getPeriodLength() {
        return JBUI.scale(16);
    }

    private static boolean isOdd(int value) {
        return value % 2 != 0;
    }
}
