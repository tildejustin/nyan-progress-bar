package tokyo.northside.intellij.plugins;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.UIManager;

public class NyanApplicationComponent implements LafManagerListener {
    public NyanApplicationComponent() {
        updateProgressBarUi();
    }

    @Override
    public void lookAndFeelChanged(@NotNull LafManager lafManager) {
        // see https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html
        updateProgressBarUi();
    }

    private void updateProgressBarUi() {
        UIManager.put("ProgressBarUI", NyanProgressBarUi.class.getName());
        UIManager.getDefaults().put(NyanProgressBarUi.class.getName(), NyanProgressBarUi.class);
    }
}
