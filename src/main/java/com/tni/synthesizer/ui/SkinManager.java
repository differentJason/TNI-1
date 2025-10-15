package com.tni.synthesizer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * UI Skin Manager for TNI Synthesizer
 * Allows switching between different interface styles
 */
public class SkinManager {
    
    public enum SkinType {
        DEFAULT("Default Swing"),
        HARDWARE("Hardware Mixer"),
        MODERN("Modern Dark");
        
        private final String displayName;
        
        SkinType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private static SkinType currentSkin = SkinType.DEFAULT;
    private static Map<SkinType, Supplier<JPanel>> skinFactories = new HashMap<>();
    private static Map<String, Object> skinParameters = new HashMap<>();
    
    static {
        // Register skin factories
        skinFactories.put(SkinType.DEFAULT, () -> createDefaultPanel());
        skinFactories.put(SkinType.HARDWARE, () -> HardwareStyleUI.createHardwareMixerPanel(8));
        skinFactories.put(SkinType.MODERN, () -> createModernPanel());
    }
    
    /**
     * Get the current skin type
     */
    public static SkinType getCurrentSkin() {
        return currentSkin;
    }
    
    /**
     * Set the current skin and return the new panel
     */
    public static JPanel setSkin(SkinType skinType) {
        currentSkin = skinType;
        return createPanelForCurrentSkin();
    }
    
    /**
     * Create panel for current skin
     */
    public static JPanel createPanelForCurrentSkin() {
        Supplier<JPanel> factory = skinFactories.get(currentSkin);
        if (factory != null) {
            return factory.get();
        }
        return createDefaultPanel();
    }
    
    /**
     * Create skin selector component
     */
    public static JPanel createSkinSelector(ActionListener onSkinChange) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("UI Style:"));
        
        JComboBox<SkinType> skinCombo = new JComboBox<>(SkinType.values());
        skinCombo.setSelectedItem(currentSkin);
        skinCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SkinType) {
                    setText(((SkinType) value).getDisplayName());
                }
                return this;
            }
        });
        
        skinCombo.addActionListener(e -> {
            SkinType selected = (SkinType) skinCombo.getSelectedItem();
            if (selected != null && selected != currentSkin) {
                setSkin(selected);
                if (onSkinChange != null) {
                    onSkinChange.actionPerformed(new ActionEvent(skinCombo, ActionEvent.ACTION_PERFORMED, "skinChanged"));
                }
            }
        });
        
        panel.add(skinCombo);
        return panel;
    }
    
    /**
     * Create default Swing panel
     */
    private static JPanel createDefaultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIManager.getColor("Panel.background"));
        
        JLabel label = new JLabel("Default Swing UI", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create modern dark panel
     */
    private static JPanel createModernPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 35));
        
        JLabel label = new JLabel("Modern Dark UI", SwingConstants.CENTER);
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Set skin parameter
     */
    public static void setSkinParameter(String key, Object value) {
        skinParameters.put(key, value);
    }
    
    /**
     * Get skin parameter
     */
    public static Object getSkinParameter(String key) {
        return skinParameters.get(key);
    }
    
    /**
     * Get skin parameter with default
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSkinParameter(String key, T defaultValue) {
        Object value = skinParameters.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}