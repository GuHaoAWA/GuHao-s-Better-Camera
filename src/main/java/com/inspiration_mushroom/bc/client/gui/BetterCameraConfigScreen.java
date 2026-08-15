package com.inspiration_mushroom.bc.client.gui;

import com.inspiration_mushroom.bc.config.BetterCameraConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

public final class BetterCameraConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.better_camera.config.title");
    private static final int LIST_TOP = 36;
    private static final int FOOTER_HEIGHT = 36;

    private final Screen parent;

    private boolean enabled;
    private boolean applyInFirstPerson;
    private boolean applyInThirdPerson;
    private boolean applyPositionDamping;
    private double fastResponseTime;
    private double bodyResponseTime;
    private double chaseResponseTime;
    private double fastResponseWeight;
    private double chaseResponseWeight;
    private double positionResponseTime;
    private double verticalResponseMultiplier;

    private OptionsList optionsList;

    public BetterCameraConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.loadCurrentValues();
    }

    @Override
    protected void init() {
        this.optionsList = new OptionsList(
                this.minecraft,
                this.width,
                this.height,
                LIST_TOP,
                this.height - FOOTER_HEIGHT,
                25
        );

        OptionInstance<Boolean> enabledOption = booleanOption(
                "option.better_camera.enabled",
                "option.better_camera.enabled.tooltip",
                this.enabled,
                value -> this.enabled = value
        );
        OptionInstance<Boolean> thirdPersonOption = booleanOption(
                "option.better_camera.apply_in_third_person",
                "option.better_camera.apply_in_third_person.tooltip",
                this.applyInThirdPerson,
                value -> this.applyInThirdPerson = value
        );
        OptionInstance<Boolean> firstPersonOption = booleanOption(
                "option.better_camera.apply_in_first_person",
                "option.better_camera.apply_in_first_person.tooltip",
                this.applyInFirstPerson,
                value -> this.applyInFirstPerson = value
        );
        OptionInstance<Boolean> positionDampingOption = booleanOption(
                "option.better_camera.apply_position_damping",
                "option.better_camera.apply_position_damping.tooltip",
                this.applyPositionDamping,
                value -> this.applyPositionDamping = value
        );

        OptionInstance<Double> fastResponseOption = timeOption(
                "option.better_camera.fast_response_time",
                "option.better_camera.fast_response_time.tooltip",
                this.fastResponseTime,
                BetterCameraConfig.FAST_RESPONSE_TIME_MIN,
                BetterCameraConfig.FAST_RESPONSE_TIME_MAX,
                value -> this.fastResponseTime = value
        );
        OptionInstance<Double> bodyResponseOption = timeOption(
                "option.better_camera.body_response_time",
                "option.better_camera.body_response_time.tooltip",
                this.bodyResponseTime,
                BetterCameraConfig.BODY_RESPONSE_TIME_MIN,
                BetterCameraConfig.BODY_RESPONSE_TIME_MAX,
                value -> this.bodyResponseTime = value
        );
        OptionInstance<Double> chaseResponseOption = timeOption(
                "option.better_camera.chase_response_time",
                "option.better_camera.chase_response_time.tooltip",
                this.chaseResponseTime,
                BetterCameraConfig.CHASE_RESPONSE_TIME_MIN,
                BetterCameraConfig.CHASE_RESPONSE_TIME_MAX,
                value -> this.chaseResponseTime = value
        );
        OptionInstance<Double> positionResponseOption = timeOption(
                "option.better_camera.position_response_time",
                "option.better_camera.position_response_time.tooltip",
                this.positionResponseTime,
                BetterCameraConfig.POSITION_RESPONSE_TIME_MIN,
                BetterCameraConfig.POSITION_RESPONSE_TIME_MAX,
                value -> this.positionResponseTime = value
        );
        OptionInstance<Double> fastWeightOption = percentageOption(
                "option.better_camera.fast_response_weight",
                "option.better_camera.fast_response_weight.tooltip",
                this.fastResponseWeight,
                value -> this.fastResponseWeight = value
        );
        OptionInstance<Double> chaseWeightOption = percentageOption(
                "option.better_camera.chase_response_weight",
                "option.better_camera.chase_response_weight.tooltip",
                this.chaseResponseWeight,
                value -> this.chaseResponseWeight = value
        );
        OptionInstance<Double> verticalMultiplierOption = doubleOption(
                "option.better_camera.vertical_response_multiplier",
                "option.better_camera.vertical_response_multiplier.tooltip",
                "option.better_camera.value.multiplier",
                this.verticalResponseMultiplier,
                BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER_MIN,
                BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER_MAX,
                value -> this.verticalResponseMultiplier = value
        );

        this.optionsList.addSmall(enabledOption, thirdPersonOption);
        this.optionsList.addSmall(firstPersonOption, positionDampingOption);
        this.optionsList.addSmall(fastResponseOption, bodyResponseOption);
        this.optionsList.addSmall(chaseResponseOption, positionResponseOption);
        this.optionsList.addSmall(fastWeightOption, chaseWeightOption);
        this.optionsList.addBig(verticalMultiplierOption);
        this.addWidget(this.optionsList);

        int buttonY = this.height - 28;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.better_camera.config.reset"),
                button -> this.resetToDefaults()
        ).bounds(this.width / 2 - 155, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.better_camera.config.cancel"),
                button -> this.closeWithoutSaving()
        ).bounds(this.width / 2 - 50, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.better_camera.config.save"),
                button -> this.saveAndClose()
        ).bounds(this.width / 2 + 55, buttonY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.optionsList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.closeWithoutSaving();
    }

    private void loadCurrentValues() {
        this.enabled = BetterCameraConfig.ENABLED.get();
        this.applyInFirstPerson = BetterCameraConfig.APPLY_IN_FIRST_PERSON.get();
        this.applyInThirdPerson = BetterCameraConfig.APPLY_IN_THIRD_PERSON.get();
        this.applyPositionDamping = BetterCameraConfig.APPLY_POSITION_DAMPING.get();
        this.fastResponseTime = BetterCameraConfig.FAST_RESPONSE_TIME.get();
        this.bodyResponseTime = BetterCameraConfig.BODY_RESPONSE_TIME.get();
        this.chaseResponseTime = BetterCameraConfig.CHASE_RESPONSE_TIME.get();
        this.fastResponseWeight = BetterCameraConfig.FAST_RESPONSE_WEIGHT.get();
        this.chaseResponseWeight = BetterCameraConfig.CHASE_RESPONSE_WEIGHT.get();
        this.positionResponseTime = BetterCameraConfig.POSITION_RESPONSE_TIME.get();
        this.verticalResponseMultiplier = BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER.get();
    }

    private void resetToDefaults() {
        this.enabled = BetterCameraConfig.ENABLED.getDefault();
        this.applyInFirstPerson = BetterCameraConfig.APPLY_IN_FIRST_PERSON.getDefault();
        this.applyInThirdPerson = BetterCameraConfig.APPLY_IN_THIRD_PERSON.getDefault();
        this.applyPositionDamping = BetterCameraConfig.APPLY_POSITION_DAMPING.getDefault();
        this.fastResponseTime = BetterCameraConfig.FAST_RESPONSE_TIME.getDefault();
        this.bodyResponseTime = BetterCameraConfig.BODY_RESPONSE_TIME.getDefault();
        this.chaseResponseTime = BetterCameraConfig.CHASE_RESPONSE_TIME.getDefault();
        this.fastResponseWeight = BetterCameraConfig.FAST_RESPONSE_WEIGHT.getDefault();
        this.chaseResponseWeight = BetterCameraConfig.CHASE_RESPONSE_WEIGHT.getDefault();
        this.positionResponseTime = BetterCameraConfig.POSITION_RESPONSE_TIME.getDefault();
        this.verticalResponseMultiplier = BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER.getDefault();
        this.rebuildWidgets();
    }

    private void saveAndClose() {
        BetterCameraConfig.ENABLED.set(this.enabled);
        BetterCameraConfig.APPLY_IN_FIRST_PERSON.set(this.applyInFirstPerson);
        BetterCameraConfig.APPLY_IN_THIRD_PERSON.set(this.applyInThirdPerson);
        BetterCameraConfig.APPLY_POSITION_DAMPING.set(this.applyPositionDamping);
        BetterCameraConfig.FAST_RESPONSE_TIME.set(this.fastResponseTime);
        BetterCameraConfig.BODY_RESPONSE_TIME.set(this.bodyResponseTime);
        BetterCameraConfig.CHASE_RESPONSE_TIME.set(this.chaseResponseTime);
        BetterCameraConfig.FAST_RESPONSE_WEIGHT.set(this.fastResponseWeight);
        BetterCameraConfig.CHASE_RESPONSE_WEIGHT.set(this.chaseResponseWeight);
        BetterCameraConfig.POSITION_RESPONSE_TIME.set(this.positionResponseTime);
        BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER.set(this.verticalResponseMultiplier);
        BetterCameraConfig.ENABLED.save();
        this.minecraft.setScreen(this.parent);
    }

    private void closeWithoutSaving() {
        this.minecraft.setScreen(this.parent);
    }

    private static OptionInstance<Boolean> booleanOption(
            String translationKey,
            String tooltipKey,
            boolean initialValue,
            Consumer<Boolean> update
    ) {
        return OptionInstance.createBoolean(
                translationKey,
                OptionInstance.cachedConstantTooltip(Component.translatable(tooltipKey)),
                initialValue,
                update
        );
    }

    private static OptionInstance<Double> timeOption(
            String translationKey,
            String tooltipKey,
            double initialValue,
            double minimum,
            double maximum,
            Consumer<Double> update
    ) {
        return doubleOption(
                translationKey,
                tooltipKey,
                "option.better_camera.value.milliseconds",
                initialValue,
                minimum,
                maximum,
                update
        );
    }

    private static OptionInstance<Double> percentageOption(
            String translationKey,
            String tooltipKey,
            double initialValue,
            Consumer<Double> update
    ) {
        return doubleOption(
                translationKey,
                tooltipKey,
                "option.better_camera.value.percentage",
                initialValue,
                BetterCameraConfig.RESPONSE_WEIGHT_MIN,
                BetterCameraConfig.RESPONSE_WEIGHT_MAX,
                update
        );
    }

    private static OptionInstance<Double> doubleOption(
            String translationKey,
            String tooltipKey,
            String valueTranslationKey,
            double initialValue,
            double minimum,
            double maximum,
            Consumer<Double> update
    ) {
        return new OptionInstance<>(
                translationKey,
                OptionInstance.cachedConstantTooltip(Component.translatable(tooltipKey)),
                (caption, value) -> Component.translatable(
                        valueTranslationKey,
                        caption,
                        formattedValue(valueTranslationKey, value)
                ),
                OptionInstance.UnitDouble.INSTANCE.xmap(
                        normalized -> minimum + normalized * (maximum - minimum),
                        value -> (value - minimum) / (maximum - minimum)
                ),
                initialValue,
                update
        );
    }

    private static Object formattedValue(String valueTranslationKey, double value) {
        if ("option.better_camera.value.milliseconds".equals(valueTranslationKey)) {
            return Math.round(value * 1000.0D);
        }
        if ("option.better_camera.value.percentage".equals(valueTranslationKey)) {
            return Math.round(value * 100.0D);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
