package com.osrshelper.agent;

import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.client.util.Text;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of MenuActionService for programmatic menu entry interaction.
 * Uses agent instrumentation to access and invoke RuneLite menu entries.
 */
public class MenuActionServiceImpl implements MenuActionService {
    private static final Logger logger = Logger.getLogger(MenuActionServiceImpl.class.getName());
    private final Client client;
    private final ServiceRegistry serviceRegistry;

    public MenuActionServiceImpl(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.client = serviceRegistry.get(Client.class);
    }

    @Override
    public boolean invokeMenuAction(String option, String target) {
        Optional<MenuEntry> entryOpt = findMenuEntry(option, target).map(o -> (MenuEntry) o);
        if (entryOpt.isPresent()) {
            MenuEntry entry = entryOpt.get();
            try {
                if (entry.onClick() != null) {
                    logger.info("Invoking onClick callback for menu entry: " + option + " | " + target);
                    entry.onClick().accept(entry);
                    return true;
                } else {
                    // Fallback: simulate click by calling client.menuAction (reflection)
                    logger.info("No onClick callback, attempting to invoke menu action via reflection for: " + option + " | " + target);
                    Method menuActionMethod = client.getClass().getMethod(
                        "menuAction",
                        int.class, int.class, int.class, int.class, String.class, String.class
                    );
                    menuActionMethod.invoke(
                        client,
                        entry.getParam0(),
                        entry.getParam1(),
                        entry.getType().getId(),
                        entry.getIdentifier(),
                        entry.getOption(),
                        entry.getTarget()
                    );
                    return true;
                }
            } catch (Exception e) {
                logger.severe("Failed to invoke menu action: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            logger.warning("No matching menu entry found for: " + option + " | " + target);
        }
        return false;
    }

    public Optional<Object> findMenuEntry(String option, String target, Object actionType) {
        if (client == null) {
            logger.severe("Client instance is null in MenuActionServiceImpl");
            return Optional.empty();
        }
        MenuEntry[] entries = client.getMenuEntries();
        String optionNorm = Text.removeTags(option).toLowerCase();
        String targetNorm = Text.removeTags(target).toLowerCase();
        for (MenuEntry entry : entries) {
            String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
            String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
            boolean typeMatches = true;
            if (actionType != null) {
                typeMatches = entry.getType().equals(actionType);
            }
            // Allow partial matches: option/target must be contained in the entry (or vice versa)
            boolean optionMatch = entryOption.contains(optionNorm) || optionNorm.contains(entryOption);
            boolean targetMatch = entryTarget.contains(targetNorm) || targetNorm.contains(entryTarget);
            if (optionMatch && targetMatch && typeMatches) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> findMenuEntry(String option, String target) {
        return findMenuEntry(option, target, null);
    }
}
