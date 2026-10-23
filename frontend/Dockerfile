# ---- build stage ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build

# ---- runtime stage: static files + reverse proxy to the services ----
FROM nginx:1.27-alpine
# Rendered to /etc/nginx/conf.d/default.conf at startup, substituting the upstream URLs below.
COPY nginx.conf /etc/nginx/templates/default.conf.template
ENV AUTH_SERVICE_URL=http://auth-service:8081 \
    POKEMON_SERVICE_URL=http://pokemon-service:8082
COPY proxy_common.conf /etc/nginx/proxy_common.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
