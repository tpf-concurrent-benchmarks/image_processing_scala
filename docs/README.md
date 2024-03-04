# Benchmarks

## Measurements

### FaMAF Server

| Measurement             | 2 Nodes            | 4 Nodes            | 6 Nodes             |
|-------------------------|--------------------|--------------------|---------------------|
| Combined Throughput     | 117 Results/Second | 139 Results/Second | 162 Results/Second  |
| Max Work Time Variation | 0.8%               | 3.2%               | 2.7%                |
| Max Memory Usage        | 830 MB/Worker      | 715 MB/Worker      | 660 MB/Worker       |
| Max Network Usage (Tx)  | 24 KB/(s * Worker) | 15 KB/(s * Worker) | 12 KB/(s * Worker)  |
| Max Network Usage (Rx)  | 18 KB/(s * Worker) | 11 KB/(s * Worker) | 8.9 KB/(s * Worker) |
| CPU Usage (Format)      | 85%                | 55%                | 40%                 |
| CPU Usage (Resolution)  | 65%                | 38%                | 30%                 |
| CPU Usage (Size)        | 19%                | 10%                | 8%                  |
| Completion Time         | 38.4 Minutes       | 32.3 Minutes       | 27.7 Minutes        |

### Cloud (GCP)

| Measurement             | 2 Nodes             | 4 Nodes             | 6 Nodes             |
|-------------------------|---------------------|---------------------|---------------------|
| Combined Throughput     | 75.3 Results/Second | 109 Results/Second  | 153 Results/Second  |
| Max Work Time Variation | 4.3%                | 2.4%                | 8.6%                |
| Max Memory Usage        | 330 MB/Worker       | 240 MB/Worker       | 200 MB/Worker       |
| Max Network Usage (Tx)  | 15 KB/(s * Worker)  | 12 KB/(s * Worker)  | 11 KB/(s * Worker)  |
| Max Network Usage (Rx)  | 11 KB/(s * Worker)  | 8.4 KB/(s * Worker) | 8.0 KB/(s * Worker) |
| CPU Usage (Format)      | 64%                 | 44%                 | 42%                 |
| CPU Usage (Resolution)  | 37%                 | 28%                 | 27%                 |
| CPU Usage (Size)        | 13%                 | 8%                  | 9%                  |
| Completion Time         | 19.9 Minutes        | 13.7 Minutes        | 9.8 Minutes         |

Average measurements using the [specified configuration](measurements/README.md)
