package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            throw new RuntimeException(
                    "No commands specified!");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                // init command has only one argument
                validateNumArgs("init", args, 1);
                // Run the init command
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs("add", args, 2);
                String filename = args[1];
                Repository.add(filename);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                //TODO
                validateNumArgs("commit", args, 2);
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                String removalFileName = args[1];
                Repository.rm(removalFileName);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
//                Repository.globalLog();
                break;
            case "find":
                validateNumArgs("global-log", args, 2);
                String findMessage = args[1];
//                Repository.find(findMessage);
                break;
            case "status":
                validateNumArgs("add", args, 1);
                Repository.status();
                break;
            case "checkout":
                validateNumArgs("checkout", args, 2, 3, 4);
                validateCheckoutArgs(args);
                int numArgs = args.length;
                switch(numArgs) {
                    case 2:
                        // Usage 3: git checkout -- [file name]

                        break;
                    case 3:
                        // Usage 1: git checkout [commit id] -- [file name]
                        break;
                    case 4:
                        // Usage 2
                        break;
                }
                String branchName = args[1];
//                Repository.checkout(branchName);
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                String newBranchName = args[1];
                Repository.branch(newBranchName);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                String removalBranchName = args[1];
                Repository.rmBranch(removalBranchName);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                String resetCommitID = args[1];
                Repository.reset(resetCommitID);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                String mergeBranchName = args[1];
                Repository.reset(mergeBranchName);
                break;
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int... n) {
        for (int i: n) {
            if (args.length == i) {
               return;
            }
        }
        throw new RuntimeException(
                String.format("Invalid number of arguments for: %s.", cmd));

    }


    public static void validateCheckoutArgs(String[] args) {
        int numArgs = args.length;
        switch (numArgs) {
            case 3:
                if (!args[1].equals("--")) {
                    throw new RuntimeException(
                            "Wrong way of using checkout command.");
                }
                break;
            case 4:
                if (!args[2].equals("--")) {
                    throw new RuntimeException(
                            "Wrong way of using checkout command.");
                }
                break;
        }
    }
}
