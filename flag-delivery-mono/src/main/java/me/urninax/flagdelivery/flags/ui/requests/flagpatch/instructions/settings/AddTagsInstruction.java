package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions.SettingInstruction;

import java.util.Set;

@Getter
public class AddTagsInstruction extends SettingInstruction{
    @Valid
    @NotEmpty(message = "values cannot be empty.")
    @Size(max = 20, message = "Feature flag can have max. 20 tags.")
    @JsonProperty("tags")
    private Set<
            @Size(max = 64, message = "Tags should be at most 64 chars.")
            @Pattern(regexp = "^[A-Za-z0-9._-]{1,64}$", message = "Tags should contain only letters, digits, '.', '-', '_'")
                    String> tags;
}
