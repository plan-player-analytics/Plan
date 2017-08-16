package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.ui.html.Html;

import java.io.Serializable;
import java.util.*;

/**
 * This is an abstract class that can be used to add data from a plugin to the
 * "Plugins"-tab of Analysis and Inspect pages.
 * <p>
 * API-section of documentation has examples on the usage of this class and how
 * to register objects extending this class.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public abstract class PluginData {

    /**
     * A list containing the AnalysisType enums that determine what should be
     * done with the data on the analysis page.
     */
    protected final List<AnalysisType> analysisTypes;
    /**
     * Placeholder string, for example "stepsTaken". This will be used when
     * building the structure of the Plugins tab.
     * <p>
     * The complete placeholder also includes the plugin name and if analysis is
     * run, a modifier.
     * <p>
     * Second parameter of any super constructor.
     */
    protected final String placeholder;
    /**
     * Name of the plugin the data is coming from.
     * <p>
     * All sources of data with the same sourcePlugin will be placed in the same
     * "box" in the "Plugins" tab.
     * <p>
     * A box has a max height of 600px, and higher than that will add a
     * scrollbar.
     * <p>
     * First parameter of any super constructor.
     */
    protected final String sourcePlugin;
    /**
     * Determines if the datapoint should only be used for the analysis page.
     * <p>
     * If set to false, the datapoint will be added to the inspect page as well.
     */
    protected boolean analysisOnly;
    /**
     * Font Awesome icon name.
     * <p>
     * http://fontawesome.io/icons/
     */
    protected String icon;
    /**
     * Prefix shown before the data, for example "Steps taken: ".
     */
    protected String prefix;
    /**
     * Suffix shown after the data, for example " steps".
     */
    protected String suffix;

    /**
     * Main constructor.
     * <p>
     * Defaults analysisOnly to true.
     * <p>
     * Defaults icon, prefix and suffix to "".
     *
     * @param sourcePlugin  Name of the plugin the data is coming from
     * @param placeholder   Placeholder string, for example "stepsTaken"
     * @param analysisTypes A list containing the AnalysisType enums that
     *                      determine what should be done with the data on the analysis page
     */
    public PluginData(String sourcePlugin, String placeholder, List<AnalysisType> analysisTypes) {
        this.placeholder = placeholder;
        this.sourcePlugin = sourcePlugin;
        analysisOnly = true;
        this.analysisTypes = analysisTypes;
        this.icon = "";
        this.prefix = "";
        this.suffix = "";
    }

    /**
     * Constructor for accepting single, multiple and arrays of AnalysisType.
     *
     * @param sourcePlugin  Name of the plugin the data is coming from
     * @param placeholder   Placeholder string, for example "stepsTaken"
     * @param analysisTypes AnalysisType enums that determine what should be
     *                      done with the data on the analysis page
     */
    public PluginData(String sourcePlugin, String placeholder, AnalysisType... analysisTypes) {
        this(sourcePlugin, placeholder, Arrays.asList(analysisTypes));
    }

    /**
     * Constructor for Inspect-page only data point.
     * <p>
     * analysisOnly will be set to false.
     *
     * @param sourcePlugin Name of the plugin the data is coming from
     * @param placeholder  Placeholder string, for example "stepsTaken"
     */
    public PluginData(String sourcePlugin, String placeholder) {
        this(sourcePlugin, placeholder, new ArrayList<>());
        analysisOnly = false;
    }

    /**
     * Returns the list of AnalysisTypes.
     * <p>
     * Used by Analysis
     *
     * @return a list.
     */
    public final List<AnalysisType> getAnalysisTypes() {
        return analysisTypes;
    }

    /**
     * This method should be used with the return values of
     * getHtmlReplaceValue(String, UUID).
     * <p>
     * It will add the div, icon, modifier, prefix and suffix to the value.
     * Modifier is for example, if calculating AnalysisType.INT_AVG "Average ",
     * it is a text that helps user understand that a calculation has been made.
     *
     * @param modifier For example "Average " - Determined by value of
     *                 AnalysisType's modifier-variable.
     * @param contents The data, number/string/html that should be placed on the
     *                 page.
     * @return a proper format for the html.
     * @see AnalysisType
     */
    public final String parseContainer(String modifier, String contents) {
        return "<div class=\"plugin-data\">" + (icon.isEmpty() ? "<br>" : Html.FONT_AWESOME_ICON.parse(icon)) + " " + modifier + prefix + contents + suffix + "</div>";
    }

    /**
     * Used to get the full placeholder.
     * <p>
     * Used to avoid conflicts with existing placeholders and placeholders of
     * other plugins.
     *
     * @param modifier Modifier determined by AnalysisType's
     *                 placeholderModifier-variable.
     * @return for example "%StepCounter_stepsTaken_total%"
     * @see AnalysisType
     */
    public final String getPlaceholder(String modifier) {
        return "%" + sourcePlugin + "_" + placeholder + modifier + "%";
    }

    /**
     * Used to get the source plugin's name.
     *
     * @return for example "StepCounter"
     */
    public final String getSourcePlugin() {
        return sourcePlugin;
    }

    /**
     * Used to get the string for the html page.
     * <p>
     * parseContainer(modifierPrefix, value); should be used for all return
     * values so that div, icon, prefix and suffix are added.
     * <p>
     * This method is used when AnalysisType.HTML is set, or while getting the
     * value for the inspect page.
     * <p>
     * When using AnalysisType.HTML a random UUID is given, so it should be
     * disregarded. modifierPrefix is empty in that case.
     *
     * @param modifierPrefix Modifier determined by AnalysisType's
     *                       modifier-variable.
     * @param uuid           UUID of the player or random UUID if AnalysisType.HTML is
     *                       used.
     * @return html for the page.
     */
    public abstract String getHtmlReplaceValue(String modifierPrefix, UUID uuid);

    /**
     * Used to get the value for analysis. The return value is determined by
     * AnalysisType you have specified. If the AnalysisType's name has a BOOLEAN
     * in it, Analysis will expect boolean values etc.
     * <p>
     * If the Type and return value mismatch, exception is thrown and the result
     * on the analysis page will say that error occurred as the value.
     * <p>
     * If a player has no value a -1 should be returned in the case of a Number.
     * -1 is excluded from the Average calculation's size and total.
     *
     * @param uuid UUID of the player the value belongs to.
     * @return Long, Integer, Double, Boolean or String, return -1 if the player
     * has no value.
     * @throws UnsupportedOperationException if implementing class has not overridden the method.
     */
    public abstract Serializable getValue(UUID uuid);

    public Map<UUID, Serializable> getValues(Collection<UUID> uuids) {
        throw new UnsupportedOperationException("Not overridden.");
    }

    /**
     * Used to set the Font Awesome icon.
     *
     * @param iconName Icon's name http://fontawesome.io/icons/
     */
    public final void setIcon(String iconName) {
        this.icon = iconName + " ";
    }

    /**
     * Used to set the analysisOnly parameter.
     * <p>
     * true: only used for Analysis page false: used for both if AnalysisTypes
     * specified, if no AnalysisTypes are specified only used for Inspect page.
     *
     * @param analysisOnly true/false
     */
    public final void setAnalysisOnly(boolean analysisOnly) {
        this.analysisOnly = analysisOnly;
    }

    /**
     * Used to get the analysisOnly parameter.
     *
     * @return true/false
     */
    public final boolean analysisOnly() {
        return analysisOnly;
    }

    /**
     * Used to get the prefix.
     *
     * @return example: "Steps Taken "
     */
    public final String getPrefix() {
        return prefix;
    }

    /**
     * Used to set the prefix.
     *
     * @param prefix for example "Steps Taken: " or a Html start tag.
     */
    public final void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Used to get the suffix.
     *
     * @return example: " steps"
     */
    public final String getSuffix() {
        return suffix;
    }

    /**
     * Used to set the suffix.
     *
     * @param suffix for example " steps" or a html end tag.
     */
    public final void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public final boolean isBanData() {
        return placeholder.contains("banned")
                && analysisTypes.contains(AnalysisType.BOOLEAN_TOTAL);
    }

    /**
     * If a PluginData object has same placeholder, sourcePlugin and
     * analysisTypes, it is considered equal.
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
        return this.analysisOnly == other.analysisOnly
                && Objects.equals(this.placeholder, other.placeholder)
                && Objects.equals(this.sourcePlugin, other.sourcePlugin)
                && Objects.equals(this.analysisTypes, other.analysisTypes);
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
