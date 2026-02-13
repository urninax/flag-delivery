package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.clauses.*;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.lifecycle.*;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.AddPrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.RemovePrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.ReplacePrerequisitesInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.prerequisites.UpdatePrerequisiteInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.rules.*;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings.*;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.AddTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.ClearTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.RemoveTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.targets.ReplaceTargetsInstruction;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.variations.*;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true
)
@JsonSubTypes({
        // General
        @JsonSubTypes.Type(value = TurnFlagOffInstruction.class, name = "turnOff"),
        @JsonSubTypes.Type(value = TurnFlagOnInstruction.class, name = "turnOn"),

        // Clauses
        @JsonSubTypes.Type(value = AddClausesInstruction.class, name = "addClause"),
        @JsonSubTypes.Type(value = AddValuesToClauseInstruction.class, name = "addValuesToClause"),
        @JsonSubTypes.Type(value = RemoveClausesInstruction.class, name = "removeClauses"),
        @JsonSubTypes.Type(value = RemoveValuesFromClauseInstruction.class, name = "removeValuesFromClause"),
        @JsonSubTypes.Type(value = UpdateClauseInstruction.class, name = "updateClause"),

        // Prerequisites
        @JsonSubTypes.Type(value = AddPrerequisiteInstruction.class, name = "addPrerequisite"),
        @JsonSubTypes.Type(value = RemovePrerequisiteInstruction.class, name = "removePrerequisite"),
        @JsonSubTypes.Type(value = ReplacePrerequisitesInstruction.class, name = "replacePrerequisites"),
        @JsonSubTypes.Type(value = UpdatePrerequisiteInstruction.class, name = "updatePrerequisite"),

        // Rules
        @JsonSubTypes.Type(value = AddRuleInstruction.class, name = "addRule"),
        @JsonSubTypes.Type(value = RemoveRuleInstruction.class, name = "removeRule"),
        @JsonSubTypes.Type(value = ReorderRulesInstruction.class, name = "reorderRules"),
        @JsonSubTypes.Type(value = ReplaceRulesInstruction.class, name = "replaceRules"),
        @JsonSubTypes.Type(value = UpdateRuleDescriptionInstruction.class, name = "updateRuleDescription"),
        @JsonSubTypes.Type(value = UpdateRuleVariationInstruction.class, name = "updateRuleVariation"),

        // Targets
        @JsonSubTypes.Type(value = AddTargetsInstruction.class, name = "addTargets"),
        @JsonSubTypes.Type(value = ClearTargetsInstruction.class, name = "clearTargets"),
        @JsonSubTypes.Type(value = RemoveTargetsInstruction.class, name = "removeTargets"),
        @JsonSubTypes.Type(value = ReplaceTargetsInstruction.class, name = "replaceTargets"),

        // Variations
        @JsonSubTypes.Type(value = AddVariationInstruction.class, name = "addVariation"),
        @JsonSubTypes.Type(value = RemoveVariationInstruction.class, name = "removeVariation"),
        @JsonSubTypes.Type(value = UpdateDefaultVariationInstruction.class, name = "updateDefaultVariation"),
        @JsonSubTypes.Type(value = UpdateFallthroughVariationInstruction.class, name = "updateFallthroughVariation"),
        @JsonSubTypes.Type(value = UpdateOffVariationInstruction.class, name = "updateOffVariation"),
        @JsonSubTypes.Type(value = UpdateVariationInstruction.class, name = "updateVariation"),

        // Lifecycle
        @JsonSubTypes.Type(value = ArchiveFlagInstruction.class, name = "archiveFlag"),
        @JsonSubTypes.Type(value = DeleteFlagInstruction.class, name = "deleteFlag"),
        @JsonSubTypes.Type(value = DeprecateFlagInstruction.class, name = "deprecateFlag"),
        @JsonSubTypes.Type(value = RestoreDeprecatedFlagInstruction.class, name = "restoreDeprecatedFlag"),
        @JsonSubTypes.Type(value = RestoreFlagInstruction.class, name = "restoreFlag"),

        // Settings
        @JsonSubTypes.Type(value = AddTagsInstruction.class, name = "addTags"),
        @JsonSubTypes.Type(value = MakeFlagPermanentInstruction.class, name = "makeFlagPermanent"),
        @JsonSubTypes.Type(value = MakeFlagTemporaryInstruction.class, name = "makeFlagTemporary"),
        @JsonSubTypes.Type(value = RemoveMaintainerInstruction.class, name = "removeMaintainer"),
        @JsonSubTypes.Type(value = RemoveTagsInstruction.class, name = "removeTags"),
        @JsonSubTypes.Type(value = UpdateDescriptionInstruction.class, name = "updateDescription"),
        @JsonSubTypes.Type(value = UpdateMaintainerMemberInstruction.class, name = "updateMaintainerMember"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseInstruction{
    private String kind;
}
