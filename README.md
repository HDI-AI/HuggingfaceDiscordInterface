# Hugginface Discord Interface


## About The Project

Creates an interface between Huggingface Inference API and the discord API.

This project allows:
* A discord bot to send message text to a huggingface inference API to any available model, send the output back to discord.
  * Ping the bot directly to start a new conversation.
  * Reply to a previous response from the bot to continue an existing conversation.

## Getting Started

The HuggingFace Discord Interface project is built using gradle. The project include a gradle wrapper so does not need to be downloaded ahead of time.
All examples assume that they will be ran in a bourne shell. If using powershell or Window's command prompt, you may need to modify the commands.

### Prerequisites

The following tools are required to begin.
* Java 17 (for example [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html))
  * `JAVA_HOME` environment variable must be set
  * `PATH` environment variable must include the bin directory from `JAVA_HOME`

### Installation

1. Create a hugginface API Key at [https://huggingface.co/docs/api-inference/quicktour](https://huggingface.co/docs/api-inference/quicktour)
2. Create a discord API Key at [https://discord.com/developers/docs/topics/oauth2](https://discord.com/developers/docs/topics/oauth2)
3. Clone the repo
   ```sh
   git clone https://github.com/HDI-AI/HuggingfaceDiscordInterface.git
   ```
4. Enter your API keys in `src/main/resources/application.properties`
   ```
   net.magswag.ai.model.token='ENTER YOUR HUGGINGFACE API TOKEN'
   net.magswag.ai.discord.token='ENTER YOUR DISCORD API TOKEN'
   ```
5. Build the project
   ```sh
   ./gradlew clean build
   ```

## Usage
* Run the application through gradle
   ```sh
   ./gradlew bootRun
   ```
* Run the application in the command line
   ```sh
   ./gradlew clean build && java -jar build/libs/HuggingfaceDiscordInterface-1.0.0.jar
   ```

## Roadmap

- [ ] Add a generic docker file and update usage for running with docker
- [ ] Add logic for the bot to leave discord servers

See the [open issues](https://github.com/HDI-AI/HuggingfaceDiscordInterface/issues) for a full list of proposed features (and known issues).


## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/yourUsername/FeatureName`)
3. Commit your Changes (`git commit -m 'A description of the change'`)
   * commit often, with the smallest possible changes in each commit.
   * each commit should compile.
4. Push to the Branch (`git push origin feature/yourUsername/FeatureName`)
5. Open a Pull Request


## License

See `LICENSE.txt` for more information.

## Contact

Project Link: [https://github.com/HDI-AI/HuggingfaceDiscordInterface](https://github.com/HDI-AI/HuggingfaceDiscordInterface)

## Acknowledgments and External Documentation

Please take time to review the following important documentation, acknowledgements, and helpful development documentation.

* [falcon-180B-chat acceptable use policy](https://huggingface.co/spaces/tiiuae/falcon-180b-license/blob/main/ACCEPTABLE_USE_POLICY.txt)
* [falcon-180B-chat license](https://huggingface.co/spaces/tiiuae/falcon-180b-license/blob/main/LICENSE.txt)
* [Huggingface Inference API Documentation](https://huggingface.co/docs/api-inference/quicktour)
* [Discord Development](https://discord.com/developers/docs/intro)
* [JDA Discord API wrapper](https://github.com/discord-jda/JDA)
* [Spring Boot](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
* [Java Docs](https://docs.oracle.com/en/java/javase/17/docs/api/)
