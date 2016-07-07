#!/usr/bin/env python

usage()
{
    echo "Usage:"
    echo "-?|-h|--help: Show this usage message"
    echo "-n <iterations>: Number of iterations to run.  If running in stablize mode, this is the number of successive iterations measured for standard deviation"
    echo "-m <iterations>: Number of iterations to run.  If running in stablize mode, this is the number of successive iterations measured for standard deviation"
    echo "-s|--stabilization: Stabilize mode.  Runs until the"
    echo "-d|--std-dev: Desired std-deviation of past : 
    echo "-m|--std-dev-length"
    exit 1
}

Iterations=10
while :; do
    if [ $# -le 0 ]; then
        break
    fi

    case $1 in
        -?|-h|--help)
            usage
            exit 1
            ;;
        -n)
            __buildmanaged=true
            ;;
        *)
            __UnprocessedBuildArgs="$__UnprocessedBuildArgs $1"
    esac

    shift
done

echo "Running native stability test for $(uname -a)"
echo "Downloading and unpacking benchmarks"

BenchMarkLocation='benchmarklocation'
curl $BenchMarkLocation -o parsec.tar.gz
tar -xf parsec.tar.gz

# Executing benchmark
for i in {1..10}; do parsecmgmt -a run -p blackscholes -i native -n 1 -c gcc-serial | grep user; done