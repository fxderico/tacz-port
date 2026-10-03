package com.tacz.guns.platform.forge.client.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.client.KeyMapping;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}
import net.minecraft.world.InteractionHand;

public class InputEvent extends Event {
    public static class Key extends InputEvent {
        private final int key, scanCode, action, modifiers;

        public Key(int key, int scanCode, int action, int modifiers) {
            this.key = key;
            this.scanCode = scanCode;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getKey() {
            return key;
        }

        public int getScanCode() {
            return scanCode;
        }

        public int getAction() {
            return action;
        }

        public int getModifiers() {
            return modifiers;
        }

        //? if >=1.21.9 {
        public KeyEvent getKeyEvent() {
            return new KeyEvent(key, scanCode, modifiers);
        }
        //?}
    }

    public static class MouseButton extends InputEvent {
        private final int button, action, modifiers;

        public MouseButton(int button, int action, int modifiers) {
            this.button = button;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getButton() {
            return button;
        }

        public int getAction() {
            return action;
        }

        public int getModifiers() {
            return modifiers;
        }

        //? if >=1.21.9 {
        public MouseButtonEvent getMouseButtonEvent() {
            return new MouseButtonEvent(0, 0, new MouseButtonInfo(button, modifiers));
        }
        //?}

        @Cancelable
        public static class Pre extends MouseButton {
            public Pre(int button, int action, int modifiers) {
                super(button, action, modifiers);
            }
        }

        public static class Post extends MouseButton {
            public Post(int button, int action, int modifiers) {
                super(button, action, modifiers);
            }
        }
    }

    @Cancelable
    public static class InteractionKeyMappingTriggered extends InputEvent {
        private final int button;
        private final KeyMapping keyMapping;
        private final InteractionHand hand;
        private boolean handSwing = true;

        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        public boolean isAttack() {
            return button == 0;
        }

        public boolean isUseItem() {
            return button == 1;
        }

        public boolean isPickBlock() {
            return button == 2;
        }

        public KeyMapping getKeyMapping() {
            return keyMapping;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public void setSwingHand(boolean value) {
            this.handSwing = value;
        }

        public boolean shouldSwingHand() {
            return handSwing;
        }
    }
}
