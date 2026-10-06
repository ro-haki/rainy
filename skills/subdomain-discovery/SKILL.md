---
name: subdomain-discovery
description: >-
  Use this skill to discover subdomains of an authorized domain. It combines passive sources,
  archived-URL mining, zone transfer, DNS brute force, DNS record probing, reverse DNS and
  permutation, then validates the results. Trigger whenever the user wants to find or enumerate
  subdomains, map a domain's attack surface, or build a list of a domain's hosts.
---

# Subdomain Discovery

Goal: build a validated list of live subdomains for an authorized domain, from several
independent sources. Start passive (no traffic to the target), then go active.

## 1. Passive enumeration — `subfinder`
Query public passive sources. Use `-all` for breadth.
- `subfinder` with `target=<domain>`, `arguments="-all"`.
- subfinder is only as complete as its configured provider API keys — more keys, more results.

## 2. Archived URLs — `gau` and `waybackurls`
Historical crawl/archive data lists hosts no longer linked anywhere. These tools emit URLs; the
subdomains are the hostnames inside those URLs.
- `gau` with `target=<domain>` (subdomains are included by default).
- `waybackurls` with `target=<domain>`.
- From each returned URL, extract the hostname and keep those ending in `.<domain>`.

## 3. Zone transfer (AXFR) — `dnsx`
A misconfigured authoritative server may hand over the entire zone — the fastest possible win.
- `dnsx` with `target=<domain>`, `arguments="-axfr -resp-only"`.
- If it succeeds, every record in the dump is a discovered subdomain.

## 4. Active brute force — `dnsx`
Resolve candidate names from a wordlist against the domain. Choose the wordlist by reasoning about
the engagement (scope, time budget, stealth) from the bundled SecLists DNS lists in
`/usr/share/seclists/Discovery/DNS/`:
- quick / stealthy → `subdomains-top1million-5000.txt`
- balanced → `subdomains-top1million-20000.txt`
- thorough → `subdomains-top1million-110000.txt` or `bitquark-subdomains-top100000.txt`
- exhaustive → `dns-Jhaddix.txt`

List the directory first and pick what fits; more wordlists may be added there over time.
Enable wildcard filtering so a wildcard record does not flood the results with false positives:
- `dnsx` with `target=<domain>`,
  `arguments="-w /usr/share/seclists/Discovery/DNS/<chosen-list> -wd <domain> -a -resp"`.
- Optionally add `-r <resolvers-file>` if a trusted resolver list is available. Keep only names that resolve.

## 5. DNS record probing (all types) — `dnsx`
For the domain and each discovered subdomain, query the full set of record types:
- `arguments="-a -aaaa -cname -ns -mx -txt -soa -resp"`.
- CNAME chains that point to decommissioned services are candidate subdomain takeovers — flag them.

## 6. Reverse DNS — `nmap`
Map the IP ranges the subdomains resolve to, then reverse-resolve the whole range to surface
sibling hosts that share infrastructure:
- `nmap` with `target=<cidr>`, `arguments="-sL -Pn"` (list scan does PTR lookups, no packets to hosts).

## 7. Permutation / mutation — `alterx` (final step)
Once passive (1–2) and active (3–6) enumeration is finished, take EVERY unique subdomain found so
far and generate likely permutations from alterx's default patterns, then resolve them:
- `alterx` with `input=<newline-separated list of all discovered subdomains>`, `arguments="-enrich"`.
- Resolve the generated candidates with `dnsx` (`arguments="-wd <domain> -a -resp"`,
  candidates via `input`) and keep only those that resolve — these are new subdomains.
- Add the new hits to the set and re-run post-processing.

## Post-processing
1. Deduplicate all discovered subdomains (`sort -u`).
2. Resolve each to confirm it is live (DNS A/AAAA record).
3. Filter out wildcard-DNS false positives (compare against a random non-existent label).
4. Categorize: live, dead, wildcard, CNAME-dangling (possible takeover).

## Rules
- Only enumerate domains the user is authorized to assess.
- Prefer passive sources (steps 1–2) first; treat zone transfer, brute force and record probing as active.
- Report the source(s) each subdomain came from so findings are reproducible.
