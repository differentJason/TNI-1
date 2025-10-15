package com.tni.synthesizer.generative;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive image panel that displays an image and allows users to select
 * different areas for sound generation with visual highlighting
 */
public class InteractiveImagePanel extends JPanel {
    
    private BufferedImage originalImage;
    private BufferedImage scaledImage;
    private Rectangle selectionArea;
    private Rectangle imageDisplayBounds;
    
    // Visual settings
    private static final Color SELECTION_COLOR = new Color(255, 255, 0, 80); // Semi-transparent yellow
    private static final Color SELECTION_BORDER = new Color(255, 255, 0, 200);
    private static final Stroke SELECTION_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    
    // Grid overlay settings
    private static final Color GRID_COLOR = new Color(255, 255, 255, 60);
    private static final int GRID_SIZE = 32; // Grid cell size in pixels
    
    // Interaction state
    private boolean isDragging = false;
    private Point dragStart;
    private Point dragEnd;
    
    // Listeners for area selection changes
    private List<AreaSelectionListener> listeners = new ArrayList<>();
    
    public interface AreaSelectionListener {
        void areaSelected(Rectangle imageArea, BufferedImage selectedRegion);
    }
    
    public InteractiveImagePanel() {
        setPreferredSize(new Dimension(400, 300));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Mouse interaction handlers
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (originalImage != null && imageDisplayBounds != null && 
                    imageDisplayBounds.contains(e.getPoint())) {
                    isDragging = true;
                    dragStart = e.getPoint();
                    dragEnd = e.getPoint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    finalizeSelection();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (originalImage != null && imageDisplayBounds != null && 
                    imageDisplayBounds.contains(e.getPoint()) && e.getClickCount() == 2) {
                    // Double-click to select entire image
                    selectEntireImage();
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && originalImage != null && imageDisplayBounds != null) {
                    dragEnd = e.getPoint();
                    
                    // Constrain to image bounds
                    dragEnd.x = Math.max(imageDisplayBounds.x, 
                               Math.min(imageDisplayBounds.x + imageDisplayBounds.width, dragEnd.x));
                    dragEnd.y = Math.max(imageDisplayBounds.y, 
                               Math.min(imageDisplayBounds.y + imageDisplayBounds.height, dragEnd.y));
                    
                    updateSelectionArea();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Update cursor based on location
                if (originalImage != null && imageDisplayBounds != null && 
                    imageDisplayBounds.contains(e.getPoint())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }
    
    /**
     * Load and display an image
     */
    public void setImage(BufferedImage image) {
        this.originalImage = image;
        if (image != null) {
            // Save current selection if any
            Rectangle savedSelection = null;
            if (selectionArea != null && imageDisplayBounds != null) {
                savedSelection = convertDisplayToImageCoordinates(selectionArea);
            }
            
            scaleImageToFit();
            
            // Restore previous selection or use default
            if (savedSelection != null) {
                restoreSelectionFromImageCoordinates(savedSelection);
            } else {
                selectDefaultArea();
            }
        } else {
            scaledImage = null;
            selectionArea = null;
            imageDisplayBounds = null;
        }
        repaint();
    }
    
    /**
     * Scale the image to fit within the panel while maintaining aspect ratio
     */
    private void scaleImageToFit() {
        if (originalImage == null) return;
        
        Dimension panelSize = getSize();
        if (panelSize.width <= 0 || panelSize.height <= 0) {
            panelSize = getPreferredSize();
        }
        
        // Calculate scaling to fit within panel with padding
        int maxWidth = panelSize.width - 20;
        int maxHeight = panelSize.height - 20;
        
        double scaleX = (double) maxWidth / originalImage.getWidth();
        double scaleY = (double) maxHeight / originalImage.getHeight();
        double scale = Math.min(scaleX, scaleY);
        
        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);
        
        // Create scaled image
        scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        // Calculate display bounds (centered)
        int x = (panelSize.width - scaledWidth) / 2;
        int y = (panelSize.height - scaledHeight) / 2;
        imageDisplayBounds = new Rectangle(x, y, scaledWidth, scaledHeight);
    }
    
    /**
     * Select a default area (center portion of the image)
     */
    private void selectDefaultArea() {
        if (imageDisplayBounds == null) return;
        
        int width = imageDisplayBounds.width / 3;
        int height = imageDisplayBounds.height / 3;
        int x = imageDisplayBounds.x + (imageDisplayBounds.width - width) / 2;
        int y = imageDisplayBounds.y + (imageDisplayBounds.height - height) / 2;
        
        selectionArea = new Rectangle(x, y, width, height);
        notifyAreaSelected();
    }
    
    /**
     * Select the entire image
     */
    private void selectEntireImage() {
        if (imageDisplayBounds == null) return;
        
        selectionArea = new Rectangle(imageDisplayBounds);
        notifyAreaSelected();
        repaint();
    }
    
    /**
     * Update selection area based on drag coordinates
     */
    private void updateSelectionArea() {
        if (dragStart == null || dragEnd == null || imageDisplayBounds == null) return;
        
        int x = Math.min(dragStart.x, dragEnd.x);
        int y = Math.min(dragStart.y, dragEnd.y);
        int width = Math.abs(dragEnd.x - dragStart.x);
        int height = Math.abs(dragEnd.y - dragStart.y);
        
        // Ensure minimum size
        width = Math.max(width, 20);
        height = Math.max(height, 20);
        
        // Constrain to image bounds
        x = Math.max(imageDisplayBounds.x, Math.min(imageDisplayBounds.x + imageDisplayBounds.width - width, x));
        y = Math.max(imageDisplayBounds.y, Math.min(imageDisplayBounds.y + imageDisplayBounds.height - height, y));
        
        selectionArea = new Rectangle(x, y, width, height);
    }
    
    /**
     * Finalize the selection and notify listeners
     */
    private void finalizeSelection() {
        if (selectionArea != null) {
            notifyAreaSelected();
        }
    }
    
    /**
     * Notify listeners of area selection
     */
    private void notifyAreaSelected() {
        if (selectionArea != null && originalImage != null && imageDisplayBounds != null) {
            Rectangle imageArea = convertDisplayToImageCoordinates(selectionArea);
            BufferedImage selectedRegion = extractImageRegion(imageArea);
            
            for (AreaSelectionListener listener : listeners) {
                listener.areaSelected(imageArea, selectedRegion);
            }
        }
    }
    
    /**
     * Convert display coordinates to original image coordinates
     */
    private Rectangle convertDisplayToImageCoordinates(Rectangle displayRect) {
        if (imageDisplayBounds == null || originalImage == null) {
            return new Rectangle();
        }
        
        // Calculate scale factor
        double scaleX = (double) originalImage.getWidth() / imageDisplayBounds.width;
        double scaleY = (double) originalImage.getHeight() / imageDisplayBounds.height;
        
        // Convert coordinates
        int x = (int) ((displayRect.x - imageDisplayBounds.x) * scaleX);
        int y = (int) ((displayRect.y - imageDisplayBounds.y) * scaleY);
        int width = (int) (displayRect.width * scaleX);
        int height = (int) (displayRect.height * scaleY);
        
        // Ensure bounds
        x = Math.max(0, Math.min(originalImage.getWidth() - 1, x));
        y = Math.max(0, Math.min(originalImage.getHeight() - 1, y));
        width = Math.min(originalImage.getWidth() - x, width);
        height = Math.min(originalImage.getHeight() - y, height);
        
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Convert image coordinates back to display coordinates
     */
    private void restoreSelectionFromImageCoordinates(Rectangle imageRect) {
        if (imageDisplayBounds == null || originalImage == null) {
            return;
        }
        
        // Calculate scale factor
        double scaleX = (double) imageDisplayBounds.width / originalImage.getWidth();
        double scaleY = (double) imageDisplayBounds.height / originalImage.getHeight();
        
        // Convert coordinates
        int x = imageDisplayBounds.x + (int) (imageRect.x * scaleX);
        int y = imageDisplayBounds.y + (int) (imageRect.y * scaleY);
        int width = (int) (imageRect.width * scaleX);
        int height = (int) (imageRect.height * scaleY);
        
        // Ensure minimum size and bounds
        width = Math.max(20, width);
        height = Math.max(20, height);
        
        x = Math.max(imageDisplayBounds.x, Math.min(imageDisplayBounds.x + imageDisplayBounds.width - width, x));
        y = Math.max(imageDisplayBounds.y, Math.min(imageDisplayBounds.y + imageDisplayBounds.height - height, y));
        
        selectionArea = new Rectangle(x, y, width, height);
        notifyAreaSelected();
    }
    
    /**
     * Extract a region from the original image
     */
    private BufferedImage extractImageRegion(Rectangle imageArea) {
        if (originalImage == null || imageArea.width <= 0 || imageArea.height <= 0) {
            return null;
        }
        
        try {
            return originalImage.getSubimage(imageArea.x, imageArea.y, imageArea.width, imageArea.height);
        } catch (Exception e) {
            System.err.println("Error extracting image region: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw image if available
        if (scaledImage != null && imageDisplayBounds != null) {
            g2d.drawImage(scaledImage, imageDisplayBounds.x, imageDisplayBounds.y, null);
            
            // Draw grid overlay
            drawGrid(g2d);
            
            // Draw selection area
            drawSelection(g2d);
        } else {
            // Draw placeholder
            drawPlaceholder(g2d);
        }
        
        g2d.dispose();
    }
    
    /**
     * Draw grid overlay on the image
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        
        // Vertical lines
        for (int x = imageDisplayBounds.x; x < imageDisplayBounds.x + imageDisplayBounds.width; x += GRID_SIZE) {
            g2d.drawLine(x, imageDisplayBounds.y, x, imageDisplayBounds.y + imageDisplayBounds.height);
        }
        
        // Horizontal lines
        for (int y = imageDisplayBounds.y; y < imageDisplayBounds.y + imageDisplayBounds.height; y += GRID_SIZE) {
            g2d.drawLine(imageDisplayBounds.x, y, imageDisplayBounds.x + imageDisplayBounds.width, y);
        }
    }
    
    /**
     * Draw selection area highlighting
     */
    private void drawSelection(Graphics2D g2d) {
        if (selectionArea == null) return;
        
        // Fill selection area
        g2d.setColor(SELECTION_COLOR);
        g2d.fillRect(selectionArea.x, selectionArea.y, selectionArea.width, selectionArea.height);
        
        // Draw selection border
        g2d.setColor(SELECTION_BORDER);
        g2d.setStroke(SELECTION_STROKE);
        g2d.drawRect(selectionArea.x, selectionArea.y, selectionArea.width, selectionArea.height);
        
        // Draw corner handles
        drawCornerHandles(g2d);
        
        // Draw info text
        drawSelectionInfo(g2d);
    }
    
    /**
     * Draw corner handles for visual feedback
     */
    private void drawCornerHandles(Graphics2D g2d) {
        g2d.setColor(SELECTION_BORDER);
        int handleSize = 6;
        
        // Top-left
        g2d.fillRect(selectionArea.x - handleSize/2, selectionArea.y - handleSize/2, handleSize, handleSize);
        
        // Top-right
        g2d.fillRect(selectionArea.x + selectionArea.width - handleSize/2, selectionArea.y - handleSize/2, handleSize, handleSize);
        
        // Bottom-left
        g2d.fillRect(selectionArea.x - handleSize/2, selectionArea.y + selectionArea.height - handleSize/2, handleSize, handleSize);
        
        // Bottom-right
        g2d.fillRect(selectionArea.x + selectionArea.width - handleSize/2, 
                    selectionArea.y + selectionArea.height - handleSize/2, handleSize, handleSize);
    }
    
    /**
     * Draw selection information
     */
    private void drawSelectionInfo(Graphics2D g2d) {
        Rectangle imageArea = convertDisplayToImageCoordinates(selectionArea);
        String info = String.format("Area: %dx%d px", imageArea.width, imageArea.height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        int textX = selectionArea.x + 5;
        int textY = selectionArea.y + fm.getAscent() + 5;
        
        // Draw text background
        Rectangle textBounds = new Rectangle(textX - 3, textY - fm.getAscent() - 2, 
                                           fm.stringWidth(info) + 6, fm.getHeight() + 4);
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(textBounds.x, textBounds.y, textBounds.width, textBounds.height);
        
        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(info, textX, textY);
    }
    
    /**
     * Draw placeholder when no image is loaded
     */
    private void drawPlaceholder(Graphics2D g2d) {
        String text = "Load an image to begin";
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
        
        // Draw border
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, 
                                     BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0));
        g2d.drawRect(10, 10, getWidth() - 20, getHeight() - 20);
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (originalImage != null) {
            SwingUtilities.invokeLater(() -> {
                // Save current selection ratio before rescaling
                Rectangle savedSelection = null;
                if (selectionArea != null && imageDisplayBounds != null) {
                    savedSelection = convertDisplayToImageCoordinates(selectionArea);
                }
                
                scaleImageToFit();
                
                // Restore selection or use default
                if (savedSelection != null) {
                    restoreSelectionFromImageCoordinates(savedSelection);
                } else {
                    selectDefaultArea();
                }
                repaint();
            });
        }
    }
    
    // Public methods
    
    public void addAreaSelectionListener(AreaSelectionListener listener) {
        listeners.add(listener);
    }
    
    public void removeAreaSelectionListener(AreaSelectionListener listener) {
        listeners.remove(listener);
    }
    
    public void clearAreaSelectionListeners() {
        listeners.clear();
    }
    
    public Rectangle getSelectedImageArea() {
        if (selectionArea != null) {
            return convertDisplayToImageCoordinates(selectionArea);
        }
        return null;
    }
    
    public BufferedImage getSelectedRegion() {
        Rectangle imageArea = getSelectedImageArea();
        if (imageArea != null) {
            return extractImageRegion(imageArea);
        }
        return null;
    }
    
    public BufferedImage getOriginalImage() {
        return originalImage;
    }
}