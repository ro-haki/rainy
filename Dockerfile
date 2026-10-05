FROM eclipse-temurin:21-jre

ENV DEBIAN_FRONTEND=noninteractive \
    PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

ARG NUCLEI_VERSION=3.3.7
ARG RUSTSCAN_VERSION=2.4.1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates curl gnupg unzip nmap masscan \
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

WORKDIR /app
COPY app/ ./
COPY skills/ ./skills/

ENTRYPOINT ["./bin/rainy"]
