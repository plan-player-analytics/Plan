/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package utilities.mocks.objects;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Collection;

/**
 * Fake console to return as mock for Bungee.
 * <p>
 * Logs with System.out.print.
 *
 * @author Rsl1122
 */
public class FakeBungeeConsole implements CommandSender {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void sendMessage(String s) {
        System.out.println(s);
    }

    @Override
    public void sendMessages(String... strings) {
        for (String string : strings) {
            sendMessage(string);
        }
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        for (BaseComponent baseComponent : baseComponents) {
            sendMessage(baseComponent);
        }
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        sendMessage(baseComponent.toPlainText());
    }

    @Override
    public Collection<String> getGroups() {
        return null;
    }

    @Override
    public void addGroups(String... strings) {

    }

    @Override
    public void removeGroups(String... strings) {

    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public void setPermission(String s, boolean b) {

    }

    @Override
    public Collection<String> getPermissions() {
        return null;
    }
}