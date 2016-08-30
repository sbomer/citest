// Import the utility functionality.

import jobs.generation.*;

def project = GithubProject
def branch = GithubBranchName
def projectFolder = Utilities.getFolderName(project) + '/' + Utilities.getFolderName(branch)

[true].each { isPR ->
    ['A'].each { letter ->
        def upstreamJob = buildFlowJob(Utilities.getFullJobName(project, "innerloop_${letter}_flow", isPR)) {
            def downstreamJobName = projectFolder + '/' + Utilities.getFullJobName(project, "innerloop_${letter}", isPR)
            buildFlow("build('$downstreamJobName')")
        }
        def newJob = job(Utilities.getFullJobName(project, "innerloop_${letter}", isPR)) {
            steps {
                batchFile("echo https://www.google.com > links1.txt")
                batchFile("echo https://www.github.com >> links1.txt")
                batchFile("echo https://www.microsoft.com >> links1.txt")
                
                batchFile("echo https://www.bing.com > links2.txt")
                batchFile("echo https://www.reddit.com >> links2.txt")
                batchFile("echo https://www.facebook.com >> links2.txt")
            }
        }
        
        // Emit summaries
        SummaryBuilder summaries = new SummaryBuilder()
        summaries.addLinksSummaryFromFile('Crash dumps from this run', 'links1.txt')
        summaries.addLinksSummaryFromFile('Other dumps from this run', 'links2.txt')
        summaries.addSummaryLinks('Static summary links', ['www.dotnet-ci.cloudapp.net', 'www.dotnet-ci.cloudapp.net/$BUILD_NUMBER'])
        summaries.emit(newJob)
        
        Utilities.setMachineAffinity(newJob, 'Windows_NT', 'latest-or-auto')
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
        if (isPR) {
            TriggerBuilder builder = TriggerBuilder.triggerOnPullRequest()
            builder.setGithubContext("Say Hello (dotnet-ci2) ${letter}")
            builder.triggerByDefault()
            builder.triggerForBranch('master')
            builder.emitTrigger(newJob)
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
        
        /*ArchivalBuilder archivalBuilder = new ArchivalBuilder()
        archivalBuilder.addFiles('links1.txt')
        archivalBuilder.addFiles('links2.txt')
        archivalBuilder.setAlwaysArchive()
        archivalBuilder.setUseAzureStorage()
        archivalBuilder.emitArchival(newJob)*/
        
        ArchivalSettings archivalSettings = new ArchivalSettings()
        archivalSettings.addFiles('links1.txt')
        archivalSettings.addFiles('links2.txt')
        archivalSettings.setAlwaysArchive()
        Utilities.addAzureArchival(newJob, 'dotnetcidata', archivalSettings)
    }
}
