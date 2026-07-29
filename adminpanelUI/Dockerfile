# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite bakes VITE_* vars into the JS bundle at build time, so they must be
# supplied as build args (not runtime env vars) — this is why the admin-ui
# image has to be rebuilt whenever the public BFF URL changes.
ARG VITE_API_BASE_URL
ARG VITE_DEMO_MODE=false
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_DEMO_MODE=${VITE_DEMO_MODE}

RUN npm run build

# --- Runtime stage -------------------------------------------------------
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY certs /etc/nginx/certs
EXPOSE 443

#HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
#  CMD wget --no-check-certificate -qO- https://localhost/admin/ || exit 1
