// Import the utility functionality.
import jobs.generation.Utilities;
import jobs.generation.JobReport;

def project = GithubProject
def branch = GithubBranchName
def gitUrl = Utilities.calculateGitURL(project)

def newJob = workflowJob('test-workflow') {
    definition {
        cps {
            script("""
                git url: '$gitUrl'
                echo 'Hello world'
            """
            )
            sandbox()
        }
    }
}