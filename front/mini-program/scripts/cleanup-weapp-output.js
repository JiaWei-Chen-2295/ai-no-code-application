const fs = require('fs')
const path = require('path')

const outputDir = path.join(__dirname, '..', 'dist', 'weapp')
const filesToRemove = ['project.config.json', 'project.private.config.json']

for (const fileName of filesToRemove) {
  const filePath = path.join(outputDir, fileName)
  if (fs.existsSync(filePath)) {
    fs.unlinkSync(filePath)
    console.log(`Removed stale ${fileName} from weapp output.`)
  }
}
