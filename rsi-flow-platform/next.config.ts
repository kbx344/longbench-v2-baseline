import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  output: 'export',
  images: {
    unoptimized: true,
    localPatterns: [
      {
        pathname: "/horsecoder-storage/**",
      },
    ],
  },
  async rewrites() {
    if (process.env.NODE_ENV !== "development") {
      return [];
    }

    return [
      {
        source: "/flowTesting-core/:path*",
        destination: "http://localhost:39910/:path*",
      }
    ];
  },
};

export default nextConfig;
