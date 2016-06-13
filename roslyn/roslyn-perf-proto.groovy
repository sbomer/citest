// TODO: input options for project/branch/etc.?  Should we even put this in a separate script at all?
// If we define script in-line or load from the environment during job creation we are a little
// less flexible in changing the workflow but a little more flexible for utilities, etc.

// Todo: replace with call to Utilities, if possible.
node ('windows && performance') {
    stage 'Check out sources'
    git url: 'https://github.com/dotnet/roslyn.git'
    stage 'Building roslyn'
    bat "cibuild.cmd /release /test64"
    stage 'Running performance tests'
    bat "Binaries\\Release\\Roslyn.Test.Performance.Runner.exe --no-trace-upload"
}