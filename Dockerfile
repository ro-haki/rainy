FROM eclipse-temurin:21-jre

ENV DEBIAN_FRONTEND=noninteractive \
    PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

ARG NUCLEI_VERSION=3.3.7
ARG RUSTSCAN_VERSION=2.4.1
ARG SUBFINDER_VERSION=2.16.0
ARG DNSX_VERSION=1.3.1
ARG GAU_VERSION=2.2.4
ARG WAYBACKURLS_VERSION=0.1.0
ARG ALTERX_VERSION=0.1.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates curl gnupg unzip git nmap masscan \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && npm install -g @playwright/mcp@latest \
    && npx playwright install --with-deps chromium \
    && npm cache clean --force \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/nuclei.zip \
        "https://github.com/projectdiscovery/nuclei/releases/download/v${NUCLEI_VERSION}/nuclei_${NUCLEI_VERSION}_linux_amd64.zip" \
    && unzip -o /tmp/nuclei.zip -d /usr/local/bin nuclei \
    && rm /tmp/nuclei.zip \
    && nuclei -update-templates

RUN curl -fsSL -o /tmp/rustscan.deb.zip \
        "https://github.com/RustScan/RustScan/releases/download/${RUSTSCAN_VERSION}/rustscan.deb.zip" \
    && unzip -o /tmp/rustscan.deb.zip -d /tmp/rustscan \
    && apt-get update \
    && apt-get install -y /tmp/rustscan/*.deb \
    && rm -rf /tmp/rustscan /tmp/rustscan.deb.zip /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/subfinder.zip \
        "https://github.com/projectdiscovery/subfinder/releases/download/v${SUBFINDER_VERSION}/subfinder_${SUBFINDER_VERSION}_linux_amd64.zip" \
    && unzip -o /tmp/subfinder.zip -d /usr/local/bin subfinder \
    && rm /tmp/subfinder.zip

RUN curl -fsSL -o /tmp/dnsx.zip \
        "https://github.com/projectdiscovery/dnsx/releases/download/v${DNSX_VERSION}/dnsx_${DNSX_VERSION}_linux_amd64.zip" \
    && unzip -o /tmp/dnsx.zip -d /usr/local/bin dnsx \
    && rm /tmp/dnsx.zip

RUN curl -fsSL -o /tmp/gau.tar.gz \
        "https://github.com/lc/gau/releases/download/v${GAU_VERSION}/gau_${GAU_VERSION}_linux_amd64.tar.gz" \
    && tar -xzf /tmp/gau.tar.gz -C /usr/local/bin gau \
    && curl -fsSL -o /tmp/waybackurls.tgz \
        "https://github.com/tomnomnom/waybackurls/releases/download/v${WAYBACKURLS_VERSION}/waybackurls-linux-amd64-${WAYBACKURLS_VERSION}.tgz" \
    && tar -xzf /tmp/waybackurls.tgz -C /usr/local/bin waybackurls \
    && rm /tmp/gau.tar.gz /tmp/waybackurls.tgz

RUN curl -fsSL -o /tmp/alterx.zip \
        "https://github.com/projectdiscovery/alterx/releases/download/v${ALTERX_VERSION}/alterx_${ALTERX_VERSION}_linux_amd64.zip" \
    && unzip -o /tmp/alterx.zip -d /usr/local/bin alterx \
    && rm /tmp/alterx.zip

# Wordlists. Only the SecLists DNS subset (sparse checkout) — the full repo is ~1GB.
RUN git clone --depth 1 --filter=blob:none --sparse \
        https://github.com/danielmiessler/SecLists.git /usr/share/seclists \
    && git -C /usr/share/seclists sparse-checkout set Discovery/DNS \
    && rm -rf /usr/share/seclists/.git

WORKDIR /app
COPY app/ ./
COPY skills/ ./skills/

EXPOSE 8080
ENTRYPOINT ["./bin/rainy"]
