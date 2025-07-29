package agents.storywriter;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MiniSciFiStorySequentialAgent {
    private static String USER_ID = "student4";
    private static String NAME = "mini_scifi_story_sequential_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        LlmAgent elementAgent = LlmAgent.builder()
                .name("Story Element Agent")
                .description("Agent that generates elements of a sci-fi story, such as a character, setting, or plot point.")
                .instruction("You are a creative agent that generates elements of a sci-fi story. " +
                        "Your task is to create a unique and imaginative element that fits within the sci-fi genre. " +
                        "Respond with only the generated element, without any additional commentary.")
                .model("gemini-2.0-flash")
                .outputKey("generated_elements")
                .build();

        LlmAgent plotAgent = LlmAgent.builder()
                .name("Plot Agent")
                .description("Agent that generates a plot for a sci-fi story based on the elements provided by the Story Element Agent.")
                .instruction("You are a plot generator for a sci-fi story. " +
                        "Your task is to create a cohesive and engaging plot that incorporates the elements provided by the Story Element Agent. " +
                        "Elements: {generated_elements}. " +
                        "Respond with only the generated plot, without any additional commentary.")
                .model("gemini-2.0-flash")
                .outputKey("generated_plot")
                .build();

        LlmAgent storyAgent = LlmAgent.builder()
                .name("Story Agent")
                .description("Agent that compiles the plot and elements into a complete sci-fi story.")
                .instruction("You are a story compiler for a sci-fi narrative. " +
                        "Your task is to create a complete story that includes the plot and elements provided by the Plot Agent. " +
                        "Element: {generated_elements}" +
                        "Plot: {generated_plot}. " +
                        "Respond with only the complete story, without any additional commentary.")
                .model("gemini-2.0-flash")
                .outputKey("story")
                .build();

        return SequentialAgent.builder()
                .name(NAME)
                .description("A sequential agent that calls agents for creating elements, plot, and compiling them into a complete story.")
                .subAgents(elementAgent, plotAgent, storyAgent)
                .build();
    }

    public static void main(String[] args) throws Exception {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session =
                runner
                        .sessionService()
                        .createSession(NAME, USER_ID)
                        .blockingGet();

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();

                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }

}
