package agents.stockanalysis;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class StockAnalysisLoopAgent {
    private static String USER_ID = "student3";
    private static String NAME = "stock_analysis_loop_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        LlmAgent analystAgent = LlmAgent.builder()
                .name("Stock Analyst")
                .description("Agent that analyzes stock data and provides insights.")
                .instruction("Analyze the stock data and provide insights for a company requested by an user.")
                .model("gemini-2.0-flash")
                .outputKey("generated_analysis")
                .build();

        LlmAgent reviewAgent = LlmAgent.builder()
                .name("Stock Analysis Reviewer")
                .description("Agent that reviews stock analysis and provides feedback." +
                        "Stock Analysis: {generated_analysis}")
                .instruction("Provide feedback on the stock analysis provided by the Stock Analyst agent.")
                .model("gemini-2.0-flash")
                .build();

        return  LoopAgent.builder()
                .name(NAME)
                .description("Repeatedly analyze stock data and review the analysis.")
                .maxIterations(3)
                .subAgents(analystAgent, reviewAgent)
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
