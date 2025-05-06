package com.osrs.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[OSRS Helper Agent] Agent started. Initializing ByteBuddy...");
        // Example: install a generic reusable hook for demonstration
        new AgentBuilder.Default()
            .type(ElementMatchers.nameContainsIgnoreCase("runelite"))
            .transform(new GenericHook(ElementMatchers.any()))
            .installOn(inst);
    }

    /**
     * Generic reusable transformer for method entry/exit hooks.
     */
    public static class GenericHook implements AgentBuilder.Transformer {
        private final ElementMatcher<? super MethodDescription> methodMatcher;

        public GenericHook(ElementMatcher<? super MethodDescription> methodMatcher) {
            this.methodMatcher = methodMatcher;
        }

        @Override
        public net.bytebuddy.dynamic.DynamicType.Builder<?> transform(
                net.bytebuddy.dynamic.DynamicType.Builder<?> builder,
                net.bytebuddy.description.type.TypeDescription typeDescription,
                ClassLoader classLoader,
                java.lang.module.Module module) {
            return builder.visit(Advice.to(GenericAdvice.class).on(methodMatcher));
        }
    }

    /**
     * Example advice for generic entry/exit logging or automation.
     */
    public static class GenericAdvice {
        @Advice.OnMethodEnter
        static void onEnter(@Advice.Origin String method) {
            // Placeholder for generic entry logic
        }
        @Advice.OnMethodExit
        static void onExit(@Advice.Origin String method) {
            // Placeholder for generic exit logic
        }
    }
}
