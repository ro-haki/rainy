---
name: port-discovery
description: >-
  Use this skill to discover open TCP ports on an authorized host and identify the services
  behind them. It runs a fast full-range sweep with rustscan or masscan, then targeted nmap
  service/version detection on only the open ports, and reports the findings. Trigger whenever
  the user wants to find or enumerate open ports, scan a host or range, map a target's attack
  surface, fingerprint services/versions, or asks what is running/listening on a machine.
---

# Port discovery

Goal: enumerate open ports and their services on a target the user is authorized to test.

## Steps

1. **Fast sweep** — find open ports across the full range quickly with `rustscan`
   (preferred) or `masscan`.
   Example: `rustscan` with `target=<host>`, `arguments="-r 1-65535 -g"`.
2. **Service/version detection** — run `nmap` against **only** the ports found in step 1.
   Example: `arguments="-sV -sC -p <comma-separated-open-ports>"`. Do not re-scan all 65535 ports with nmap.
3. **Report** — list each open port with protocol, service and version, and call out anything
   unusual (unexpected services, outdated versions, exposed admin interfaces).

## Rules

- Prefer rustscan/masscan for breadth and nmap for depth.
- Do not run `nmap` if rustscan/masscan responds that all ports are open
  - It means that port spoofer in place
- Never scan hosts outside the authorized scope.
