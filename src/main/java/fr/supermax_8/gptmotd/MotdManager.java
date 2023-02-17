package fr.supermax_8.gptmotd;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import fr.supermax_8.gptmotd.utils.CrossConfiguration;
import fr.supermax_8.gptmotd.utils.CrossConfigurationSection;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MotdManager {

    @Getter
    private static MotdManager instance;
    private String currentMotd;
    @Getter
    private final Mode mode;
    @Getter
    private long time = 5;
    @Getter
    private int random;
    private String apiKey;
    private HashMap<String, Supplier<String>> prompts = new HashMap<>();
    private String motd;
    private Consumer<Runnable> runAsync;

    @Getter
    private long lastUpdate = 0;

    public MotdManager(File configFile, Consumer<Runnable> runAsync) {
        instance = this;

        this.runAsync = runAsync;
        CrossConfiguration config = CrossConfiguration.loadConfiguration(configFile);
        List<String> keys = config.getKeys(false);
        List<String> motd = (List<String>) config.get("motd");
        StringBuilder builder = new StringBuilder();
        for (String s : motd) {
            builder.append(s).append("§r\n");
        }
        this.motd = preTextFormat(builder).toString();

        apiKey = config.getString("api-key");
        mode = Mode.valueOf(config.getString("mode").toUpperCase());
        if (keys.contains("time")) time = (int) config.get("time");
        if (keys.contains("random")) random = (int) config.get("random");

        config.getConfigurationSection("prompts").getKeys(false).forEach(s -> {
            CrossConfigurationSection section = config.getConfigurationSection("prompts." + s);
            prompts.put(s, interpretPrompt(section));
        });

        updateMotd(true);
    }

    public void updateMotd() {
        updateMotd(false);
    }

    public void updateMotd(boolean force) {
        AtomicLong time = new AtomicLong(System.currentTimeMillis());
        if (!force) {
            if (time.get() - lastUpdate < 1000 * this.time) return;
            if (new Random().nextInt(Math.max(random, 1)) != 0) return;
        }
        /*System.out.println("MOTD UPDATE...");*/
        lastUpdate = time.get();
        runAsync.accept(() -> {
            try {
                StringBuilder builder = new StringBuilder(motd);

                prompts.forEach((s, supplier) -> replaceAll(builder, s, supplier.get()));
                currentMotd = builder.toString();
                System.out.println("MOTD UPDATED ! (" + (System.currentTimeMillis() - time.get()) + "ms)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void replaceAll(StringBuilder builder, String toReplace, String replacement) {
        int toReplaceLength = toReplace.length();
        int indexToReplace = builder.indexOf(toReplace);
        while (indexToReplace != -1) {
            builder.replace(indexToReplace, indexToReplace + toReplaceLength, replacement);
            indexToReplace = builder.indexOf(toReplace);
        }
    }

    public static StringBuilder preTextFormat(StringBuilder text) {
        StringBuilder builder = new StringBuilder(text);
        replaceAll(builder, "&", "§");
        int customColorIndex = builder.indexOf("§#");
        while (customColorIndex != -1) {
            String colorText = builder.substring(customColorIndex + 1, customColorIndex + 8);
            builder.replace(customColorIndex, customColorIndex + 8, ChatColor.of(colorText) + "");
            customColorIndex = builder.indexOf("§#");
        }
        return builder;
    }

    public String getMotd() {
        return currentMotd;
    }


    private Supplier<String> interpretPrompt(CrossConfigurationSection section) {
        System.out.println("Interpreting prompt " + section.getName());
        int maxTokens = section.get("max-tokens") == null ? 20 : (int) section.get("max-tokens");
        int maxCharacterLength = section.get("max-character-length") == null ? 100 : (int) section.get("max-character-length");
        String model = (String) section.get("model");
        String customPrompt = "words about {0}:";

        try {
            String possibleNewPrompt = (String) section.get("custom-prompt");
            if (possibleNewPrompt != null && !possibleNewPrompt.isEmpty()) customPrompt = possibleNewPrompt;
        } catch (ClassCastException e) {
        }

        List<String> prompts = (List<String>) section.get("prompt");
        if (prompts != null && !prompts.isEmpty()) {
            System.out.println("-    prompt" + prompts);
            String finalCustomPrompt = customPrompt;
            return () -> {
                String prompt = finalCustomPrompt.replace("{0}", prompts.get(new Random().nextInt(prompts.size())));
                return callGpt(prompt, model, maxTokens, maxCharacterLength);
            };
        } else {
            List<String> customPrompts = (List<String>) section.get("custom-prompt");
            System.out.println("-    custom-prompt" + customPrompts);
            return () -> {
                String prompt = customPrompts.get(new Random().nextInt(customPrompts.size()));
                /*System.out.println("PromptCalled: " + prompt);*/
                return callGpt(prompt, model, maxTokens, maxCharacterLength);
            };
        }
    }

    private String callGpt(String prompt, String model, int maxTokens, int maxCharacterLength) {
        OpenAiService service = new OpenAiService(apiKey);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .model(model == null ? "text-davinci-003" : model)
                .echo(false)
                .maxTokens(maxTokens)
                .build();
        String response = service.createCompletion(completionRequest).getChoices().get(0).getText();
        StringBuilder responseBuilder = new StringBuilder(response);
        replaceAll(responseBuilder, "\n", "");
        replaceAll(responseBuilder, "\"", "");

        return responseBuilder.length() > maxCharacterLength ? responseBuilder.substring(0, maxCharacterLength) : responseBuilder.toString();
    }

    enum Mode {
        FOREACH,
        TIME
    }

}