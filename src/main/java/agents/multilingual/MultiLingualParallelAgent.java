package agents.multilingual;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.ParallelAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MultiLingualParallelAgent {
    private static String USER_ID = "student1";
    private static String NAME = "multilingual_parallel_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        LlmAgent koreanAgent = LlmAgent.builder()
                .name("Korean Agent")
                .description("You are an agent that speaks in Korean.")
                .instruction("Response in Korean to the user's input.")
                .model("gemini-2.0-flash")
                .build();
        LlmAgent japaneseAgent = LlmAgent.builder()
                .name("Japanese Agent")
                .description("You are an agent that speaks in Japanese.")
                .instruction("Response in Japanese to the user's input.")
                .model("gemini-2.0-flash")
                .build();

        LlmAgent chineseAgent = LlmAgent.builder()
                .name("Chinese Agent")
                .description("You are an agent that speaks in Chinese.")
                .instruction("Response in Chinese to the user's input.")
                .model("gemini-2.0-flash")
                .build();

        return  ParallelAgent.builder()
                        .name(NAME)
                        .description("Invoke all agents to response in multiple languages.")
                        // The agents will run in the order provided: Writer -> Reviewer -> Refactorer
                        .subAgents(koreanAgent, japaneseAgent, chineseAgent)
                        .build();
    }

    public static void main(String[] args) throws Exception {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session = runner
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
