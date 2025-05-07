<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

We are making a tool to act as a helper for Old School RuneScape for various tasks. We will be targetting the RuneLite client for this purpose, as it uses Java and this allows us more options for deep, unrestricted automation. We will use a Java agent project for injecting automation into the RuneLite client, and a custom launcher to assist with our injection.

The RuneLite source code is found in the 'runelite' folder. YOU WILL keep this in context when thinking about our implementation. YOU MUST REFER TO THIS as it will assist us in each step whilst creating our helper. DO NOT TOUCH THE RUNELITE FOLDER. Keep all of our code code separate from the RuneLite source folder.

We will place the agent code inside the 'osrs-helper-agent' folder. ONLY PLACE AGENT CODE IN THIS FOLDER. The code structure should be modular and allow for extensibility, as there will eventually be multiple tasks that the agent can run. The agent will output debug and error info to the console and to a log file called 'agent-output'. The log file should be cleared at the start of each run to avoid large file sizes. You will keep the log file in context when available so you have direct access to any debugging or error information.

We will place the launcher code inside the 'osrs-helper-launcher' folder. ONLY PLACE LAUNCHER CODE IN THIS FOLDER. The code structure should be modular and allow for extensibility. The launcher will output debug and error info to the console and to a log file called 'launcher-output'. The log file should be cleared at the start of each run to avoid large file sizes. You will keep the log file in context when available so you have direct access to any debugging or error information.

We will be using Maven to build our projects, as this is the same tool used to build the RuneLite source code. The agent and launcher should be considered separate projects, so they should be able to be built separately from each other.

The git repository for this project is in the top level folder. Please ensure that Maven output files, the 'runelite' folder and our log files are added to the .gitignore file. You will commit each change to the git repository with a comment summarising the changes.

You will ask me about the current step that we are working on. When I say a step has been completed, you will output a summary into a markdown file inside of the 'steps-taken' folder. You will need to generate a timestamp for this using 'YYYY-MM-DD HH:mm' format, which I will refer to using this placeholder - <TIMESTAMP>. When generating timestamps for step completions, ALWAYS use the system time from the machine you are running on. DO NOT use a hardcoded or context date. At the top of this file you will add 'COMPLETED - <TIMESTAMP>', making sure to replace <TIMESTAMP> as with the generated timestamp as previously instructed. You should also include the timestamp in the filename, here is an example - '2025-05-07 09-30_scaffold-osrs-helper-agent.md' Doing this will allow you to work over multiple chats, which will help keep your context small and focused.

When generating timestamps for step completions, ALWAYS use the system time from the machine you are running on. DO NOT use a hardcoded or context date.

At the start of any chat, please check the 'steps-taken' folder so you get up to speed with where we are currently at in our project. This folder will be empty at the start of our work, so please don't freak out about it.
