package main.java.com.djrapitops.plan.data.additional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import main.java.com.djrapitops.plan.ui.Html;

/**
 *
 * @author Rsl1122
 */
public abstract class PluginData {

    protected String placeholder;
    protected String sourcePlugin;
    protected boolean analysisOnly;
    protected String icon;
    protected String prefix;
    protected String suffix;
    protected List<AnalysisType> analysisTypes;

    public PluginData(String sourcePlugin, String placeholder, List<AnalysisType> analysisTypes) {
        this.placeholder = placeholder;
        this.sourcePlugin = sourcePlugin;
        analysisOnly = true;
        this.analysisTypes = analysisTypes;
        this.icon = "";
        this.prefix = "";
        this.suffix = "";
    }

    public PluginData(String sourcePlugin, String placeholder, AnalysisType... analysisTypes) {
        this(sourcePlugin, placeholder, Arrays.asList(analysisTypes));
    }

    public PluginData(String sourcePlugin, String placeholder) {
        this(sourcePlugin, placeholder, new ArrayList<>());
    }

    public final List<AnalysisType> getAnalysisTypes() {
        return analysisTypes;
    }

    public final String parseContainer(String modifier, String contents) {
        return "<div class=\"plugin-data\">" + icon + modifier + prefix + contents + suffix + "</div>";
    }

    public final String getPlaceholder(String modifier) {
        return "%" + sourcePlugin + "_" + placeholder + modifier + "%";
    }

    public final String getSourcePlugin() {
        return sourcePlugin;
    }

    public abstract String getHtmlReplaceValue(String modifierPrefix, UUID uuid);

    public abstract Serializable getValue(UUID uuid);

    public final void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public final void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public final void setIcon(String iconName) {
        this.icon = Html.FONT_AWESOME_ICON.parse(iconName) + " ";
    }

    public final void setAnalysisOnly(boolean analysisOnly) {
        this.analysisOnly = analysisOnly;
    }

    public final boolean analysisOnly() {
        return analysisOnly;
    }

    public final String getPrefix() {
        return prefix;
    }

    public final String getSuffix() {
        return suffix;
    }

    /**
     * If a PluginData object has same placeholder, sourcePlugin &
     * analysisTypes, it is considired equal.
     *
     * @param obj Another Object.
     * @return Is current object equal to given object.
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PluginData other = (PluginData) obj;
        if (this.analysisOnly != other.analysisOnly) {
            return false;
        }
        if (!Objects.equals(this.placeholder, other.placeholder)) {
            return false;
        }
        if (!Objects.equals(this.sourcePlugin, other.sourcePlugin)) {
            return false;
        }
        if (!Objects.equals(this.analysisTypes, other.analysisTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.placeholder);
        hash = 47 * hash + Objects.hashCode(this.sourcePlugin);
        hash = 47 * hash + (this.analysisOnly ? 1 : 0);
        hash = 47 * hash + Objects.hashCode(this.prefix);
        hash = 47 * hash + Objects.hashCode(this.suffix);
        hash = 47 * hash + Objects.hashCode(this.analysisTypes);
        return hash;
    }
}
