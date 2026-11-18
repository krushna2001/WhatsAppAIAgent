import express from "express";

const app = express();
app.use(express.json({ limit: "16kb" }));

app.post("/reply", async (req, res) => {
  try {
    const { sender, message } = req.body ?? {};
    if (typeof sender !== "string" || typeof message !== "string" ||
        !sender.trim() || !message.trim() || message.length > 2000) {
      return res.status(400).json({ error: "Invalid sender/message" });
    }
    if (!process.env.OPENAI_API_KEY) {
      return res.status(503).json({ error: "Server AI credentials are not configured" });
    }

    const upstream = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: process.env.OPENAI_MODEL || "gpt-6.2-astra",
        instructions: [
          "You are the user's WhatsApp auto-reply assistant.",
          "Write one short, natural, friendly reply suitable for WhatsApp.",
          "Use the language of the incoming message when possible.",
          "Do not claim the user has done, agreed to, paid, sent, or promised anything.",
          "Do not disclose private information or invent facts.",
          "If the message asks for a consequential decision, money, passwords, OTPs, medical/legal advice, or urgent help, reply that the user will respond personally soon.",
          "Return only the reply text, without quotation marks."
        ].join(" "),
        input: `Sender: ${sender}\nIncoming WhatsApp message: ${message}`
      })
    });

    const data = await upstream.json();
    if (!upstream.ok) {
      console.error("OpenAI API error", upstream.status);
      return res.status(502).json({ error: "AI provider request failed" });
    }

    const reply = (data.output ?? [])
      .flatMap(item => item.content ?? [])
      .filter(part => part.type === "output_text")
      .map(part => part.text)
      .join("\n")
      .trim();

    if (!reply) return res.status(502).json({ error: "AI returned an empty reply" });
    return res.json({ reply: reply.slice(0, 1000) });
  } catch (error) {
    console.error("Request failed", error?.message);
    return res.status(500).json({ error: "Internal server error" });
  }
});

const port = Number(process.env.PORT || 3000);
app.listen(port, () => console.log(`WA AI Agent backend listening on ${port}`));
