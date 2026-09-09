package git.artdeell.installer_agent;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Agent implements AWTEventListener {
    private boolean forgeWindowHandled = false;
    private final boolean suppressProfileCreation;
    private final boolean optiFineInstallation;
    private final String modpackFixupId;
    private final Timer componentTimer = new Timer();

    public Agent(boolean nps, boolean of, String mf) {
        this.suppressProfileCreation = !nps;
        this.optiFineInstallation = of;
        this.modpackFixupId = mf;
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        WindowEvent windowEvent = (WindowEvent) event;
        Window window = windowEvent.getWindow();
        if(windowEvent.getID() != WindowEvent.WINDOW_OPENED) return;
        if(forgeWindowHandled && window instanceof JDialog) { 
            handleDialog(window);
            return;
        }
        if(!forgeWindowHandled) { 
            forgeWindowHandled =  handleMainWindow(window);
            checkComponentTimer();
        }
    }

    public void checkComponentTimer() {
        if(forgeWindowHandled) {
            componentTimer.cancel();
            componentTimer.purge();
            return;
        }
        componentTimer.schedule(new ComponentTimeoutTask(), 30000);

    }

    public boolean handleMainWindow(Window window) {
        List<Component> components = new ArrayList<>();
        insertAllComponents(components, window, new MainWindowFilter());
        AbstractButton okButton = null;
        for(Component component : components) {
            if(component instanceof AbstractButton) {
                AbstractButton abstractButton = (AbstractButton) component;
                abstractButton = optiFineInstallation ?
                        handleOptiFineButton(abstractButton) :
                        handleForgeButton(abstractButton);
                if(abstractButton != null) okButton = abstractButton;
            }
        }
        if(okButton == null) {
            System.out.println("Failed to set all the UI components, wil try again in the next window");
            return false;
        }else{
            ProfileFixer.storeProfile(optiFineInstallation ? "OptiFine" : "forge");
            EventQueue.invokeLater(okButton::doClick); 
            return true;
        }
    }


    public AbstractButton handleForgeButton(AbstractButton abstractButton) {
        switch(abstractButton.getText()) {
            case "OK":
                return  abstractButton; 
            case "Install client":
                abstractButton.doClick(); 
        }
        return null;
    }

    public AbstractButton handleOptiFineButton(AbstractButton abstractButton) {
        if ("Install".equals(abstractButton.getText())) {
            return abstractButton;
        }
        return null;
    }

    public void handleDialog(Window window) {
        List<Component> components = new ArrayList<>();
        insertAllComponents(components, window, new DialogFilter()); 
        if(components.size() == 1) {
            
            
            
            JOptionPane optionPane = (JOptionPane) components.get(0);
            if(optionPane.getMessageType() == JOptionPane.INFORMATION_MESSAGE) { 
                System.out.println("The install was successful!");
                ProfileFixer.reinsertProfile(optiFineInstallation ? "OptiFine" : "forge", modpackFixupId, suppressProfileCreation);
                System.exit(0); 
            }
        }
    }

    public void insertAllComponents(List<Component> components, Container parent, ComponentFilter filter) {
        int componentCount = parent.getComponentCount();
        for(int i = 0; i < componentCount; i++) {
            Component component = parent.getComponent(i);
            if(filter.checkComponent(component)) components.add(component);
            if(component instanceof Container) {
                insertAllComponents(components, (Container) component, filter);
            }
        }
    }

    public static void premain(String args, Instrumentation inst) {
        boolean noProfileSuppression = false;
        boolean optifine = false;
        String modpackFixupId = null;
        if(args != null ) {
            modpackFixupId = findQuotedString(args);
            if(modpackFixupId != null) {
                noProfileSuppression = args.contains("NPS") && !modpackFixupId.contains("NPS");
                
                optifine = args.contains("OF") && !modpackFixupId.contains("OF");
                
            }else {
                noProfileSuppression = args.contains("NPS"); 
                optifine = args.contains("OF"); 
            }
        }
        Agent agent = new Agent(noProfileSuppression, optifine, modpackFixupId);
        Toolkit.getDefaultToolkit()
                .addAWTEventListener(agent,
                        AWTEvent.WINDOW_EVENT_MASK);
    }

    private static String findQuotedString(String args) {
        int quoteIndex = args.indexOf('"');
        if(quoteIndex == -1) return null;
        int nextQuoteIndex = args.indexOf('"', quoteIndex+1);
        if(nextQuoteIndex == -1) return null;
        return args.substring(quoteIndex+1, nextQuoteIndex);
    }
}
